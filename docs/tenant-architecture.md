# 多租户数据隔离架构：每用户独立 H2 文件数据库

## 一、概述

本项目采用**每用户一个独立 H2 文件数据库**的方案实现数据隔离，替代传统的单库 `ownerId` 字段过滤方案。

- **默认库**（`./data/eta.mv.db`）：仅存放用户登录凭证（`eta_user_account`），用于注册/登录认证
- **用户库**（`./data/users/{userId}.mv.db`）：存放该用户的全部业务数据（笔记、任务、错题、简历、个人档案等）
- **动态路由**：通过 `AbstractRoutingDataSource` + `ThreadLocal` 在请求级别自动切换数据源

该方案为后续**云端数据同步**奠定基础——同步即传输整个 H2 文件，数据边界天然清晰。

---

## 二、架构原理

### 2.1 核心抽象：AbstractRoutingDataSource

Spring 提供的 `AbstractRoutingDataSource` 本身是一个 `DataSource`，但内部维护一个 `Map<Object, DataSource>`。每次调用 `getConnection()` 时，它执行：

```
getConnection()
  → determineTargetDataSource()
    → determineCurrentLookupKey()   ← 子类重写此方法，返回当前租户标识
    → resolvedDataSources.get(lookupKey)
    → 返回对应的 DataSource 的 Connection
```

这是**策略模式**的典型应用：把"选哪个数据源"的决策抽象成 `determineCurrentLookupKey()` 一个方法。

### 2.2 ThreadLocal：线程级租户上下文

HTTP 请求在 Servlet 容器中由线程池处理，一个请求全程在同一个线程中执行。`ThreadLocal` 提供线程隔离的变量：

- 请求开始时 `TenantContext.set(userId)`
- 业务代码访问数据库时，`determineCurrentLookupKey()` 从 `ThreadLocal` 取 userId
- 请求结束时 `TenantContext.clear()`（防止线程池复用串数据）

### 2.3 数据分离设计

| 数据类型 | 存储位置 | 表名 | 说明 |
|---|---|---|---|
| 登录凭证 | 默认库 | `eta_user_account` | email、passwordHash、disabled |
| 个人档案 | 用户库 | `eta_user_profile` | nickname、avatar、bio |
| 笔记 | 用户库 | `eta_note` / `note_tag` / `note_note_tag` | |
| 任务 | 用户库 | `eta_task` / `eta_task_category` | |
| 错题 | 用户库 | `eta_error` / `error_tags` | 含 FSRS 复习字段 |
| 简历 | 用户库 | `eta_resume` / `resume_education` 等 | |

**为什么 UserProfile 在用户库而不是默认库？**
个人档案属于用户业务数据的一部分，云端同步时应随用户库文件一起传输。登录凭证留在默认库，因为认证时还不知道用户属于哪个库。

---

## 三、核心组件详解

### 3.1 TenantContext

**文件**：`src/main/java/cn/eta/team/eta/tenant/TenantContext.java`

线程级租户上下文，基于 `ThreadLocal<String>` 实现。

```java
public final class TenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void set(String userId) { CURRENT.set(userId); }
    public static String get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }

    // 临时切换到默认库执行代码，执行完恢复原上下文
    public static <T> T runAsDefault(Supplier<T> action) {
        String previous = CURRENT.get();
        CURRENT.remove();
        try {
            return action.get();
        } finally {
            if (previous != null) CURRENT.set(previous);
        }
    }
}
```

**关键方法**：
- `set/get/clear`：基础的 ThreadLocal 操作
- `runAsDefault(Supplier<T>)`：临时清除租户上下文，使数据库操作路由到默认库，执行完后自动恢复。用于需要访问默认库 `eta_user_account` 的场景（如修改密码、查询账号信息）

### 3.2 TenantRoutingDataSource

**文件**：`src/main/java/cn/eta/team/eta/tenant/TenantRoutingDataSource.java`

继承 `AbstractRoutingDataSource`，实现动态路由。

```java
public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();

    public TenantRoutingDataSource() {
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.get();  // 从 ThreadLocal 取当前用户 ID
    }

    // 运行时动态新增用户数据源（用户注册/登录时调用）
    public void addTenantDataSource(String userId, DataSource dataSource) {
        targetDataSources.put(userId, dataSource);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();  // 触发重新解析，使新数据源生效
    }
}
```

**路由规则**：
- `TenantContext.get()` 返回 userId → 路由到对应用户的 H2 文件库
- `TenantContext.get()` 返回 null（未登录/已清除）→ 路由到默认数据源

**`addTenantDataSource` 的三步**：
1. `put`：加入 `targetDataSources` Map
2. `setTargetDataSources`：重新设置父类引用
3. `afterPropertiesSet()`：**关键**。父类的 `resolvedDataSources` 在初始化时一次性拷贝，运行时新增数据源后必须调用此方法让父类重新解析

### 3.3 DataSourceConfig

**文件**：`src/main/java/cn/eta/team/eta/tenant/DataSourceConfig.java`

定义两个数据源 Bean。

```java
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource defaultDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(defaultUrl);  // jdbc:h2:file:./data/eta;MODE=MySQL;...
        // ...
        return ds;
    }

    @Bean
    @Primary
    public TenantRoutingDataSource routingDataSource(DataSource defaultDataSource) {
        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setDefaultTargetDataSource(defaultDataSource);
        return routing;
    }
}
```

**两个 Bean 的关系**：

```
defaultDataSource (HikariDataSource)
    │  指向 ./data/eta.mv.db（用户登录凭证）
    ▼
routingDataSource (@Primary, TenantRoutingDataSource)
    │  defaultTargetDataSource = defaultDataSource
    │  resolvedDataSources = { userId1 → HikariDS1, userId2 → HikariDS2, ... }
    ▼
被 JPA / Hibernate / 所有 Repository 注入
```

**为什么 `routingDataSource` 要 `@Primary`？**
Spring 容器中有两个 `DataSource` Bean，JPA 的 `EntityManagerFactory` 按类型注入时遇到多个会报 `NoUniqueBeanDefinitionException`。`@Primary` 告诉 Spring 优先注入路由数据源。

**返回类型为什么是 `TenantRoutingDataSource` 而不是 `DataSource`？**
`UserDatabaseInitializer` 需要注入具体类型 `TenantRoutingDataSource` 来调用 `addTenantDataSource`。如果返回类型写 `DataSource`，Spring 只注册 `DataSource` 类型的 Bean，按具体类型注入时找不到。

### 3.4 UserDatabaseInitializer

**文件**：`src/main/java/cn/eta/team/eta/tenant/UserDatabaseInitializer.java`

负责用户数据库的创建、建表和注册。

```java
@Component
public class UserDatabaseInitializer {
    private final TenantRoutingDataSource routingDataSource;

    @Value("${eta.user-db.path:./data/users}")
    private String userDbPath;

    // 创建用户库：建目录 → 创建 HikariDataSource → 执行建表脚本 → 注册到路由表
    public void createDatabase(String userId) {
        Path dir = Paths.get(userDbPath);
        Files.createDirectories(dir);

        String url = "jdbc:h2:file:" + dir.toAbsolutePath() + "/" + userId
                + ";MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        // ...

        runSchemaScript(ds);                          // 执行 schema-user.sql
        routingDataSource.addTenantDataSource(userId, ds);  // 注册到路由表
    }

    // 登录时调用：已注册则重新执行建表脚本（迁移），未注册则创建
    public void ensureDatabase(String userId) {
        if (routingDataSource.getResolvedDataSources().containsKey(userId)) {
            DataSource ds = getDataSource(userId);
            runSchemaScript(ds);
            return;
        }
        createDatabase(userId);
    }

    // 轻量版：仅确保已注册，不重复执行脚本。Filter 中每个请求调用
    public void ensureRegistered(String userId) {
        if (!routingDataSource.getResolvedDataSources().containsKey(userId)) {
            createDatabase(userId);
        }
    }

    private void runSchemaScript(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-user.sql"));
        }
    }
}
```

**三个方法的区别**：

| 方法 | 已注册时 | 未注册时 | 调用时机 |
|---|---|---|---|
| `createDatabase` | 不检查，直接创建（可能覆盖） | 创建 | 注册时 |
| `ensureDatabase` | 重新执行建表脚本（迁移） | 创建 | 登录时 |
| `ensureRegistered` | 什么都不做 | 创建 | 每个认证请求的 Filter 中 |

**H2 JDBC URL 参数说明**：

| 参数 | 含义 |
|---|---|
| `file:{path}/{userId}` | 数据库文件路径，最终生成 `{userId}.mv.db` |
| `MODE=MySQL` | H2 兼容 MySQL 语法 |
| `AUTO_SERVER=TRUE` | 允许多个进程同时打开同一文件（应用 + H2 Console） |
| `DB_CLOSE_DELAY=-1` | 最后一个连接关闭后不关闭数据库，保持缓存 |

### 3.5 SQL 脚本

**`schema.sql`**（默认库，启动时由 `DataInitializer` 手动执行）：

```sql
CREATE TABLE IF NOT EXISTS eta_user_account (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(128) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  disabled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP
);
```

**`schema-user.sql`**（用户库，用户注册/登录时执行）：

包含 `eta_user_profile`、`eta_note`、`note_tag`、`note_note_tag`、`eta_task`、`eta_task_category`、`eta_error`、`error_tags`、`eta_resume`、`resume_education`、`resume_experience`、`resume_projects`、`resume_skills` 共 13 张表，全部 `CREATE TABLE IF NOT EXISTS`。

**为什么不用 Hibernate ddl-auto？**
用户库是运行时动态创建的，Hibernate 启动时还不知道它们的存在，所以必须手动执行 SQL 脚本。默认库也改用手动脚本，避免 Hibernate 在默认库中创建多余的业务表。

### 3.6 JwtAuthenticationFilter

**文件**：`src/main/java/cn/eta/team/eta/security/JwtAuthenticationFilter.java`

在认证过滤器中设置租户上下文。

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDatabaseInitializer userDatabaseInitializer;

    @Override
    protected void doFilterInternal(...) {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(token);
                String userId = jwtService.getUserId(claims);

                userDatabaseInitializer.ensureRegistered(userId);  // 确保用户库已注册

                EtaPrincipal principal = new EtaPrincipal(userId, email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                TenantContext.set(userId);                            // 设置租户上下文
            } catch (Exception ignored) {
                // 令牌无效：保持未认证状态
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();                                   // 必须清除
        }
    }
}
```

**为什么 Filter 中要调用 `ensureRegistered`？**
JWT 是无状态的，有效期 24 小时。应用重启后内存中的路由表（`resolvedDataSources`）为空，但用户的 JWT 仍然有效。如果用户不重新登录直接发请求，`TenantContext.set(userId)` 后路由表中没有这个 userId，请求会回退到默认库（没有业务表）。

`ensureRegistered` 检查路由表，未注册则创建用户库并注册，确保每个认证请求都能正确路由。

**为什么 `finally` 中必须 `clear`？**
Servlet 容器使用线程池，请求结束后线程归还池中。如果不清除 `ThreadLocal`，下一个复用该线程的请求会继承上一个用户的 `TenantContext`，导致**串数据**。

### 3.7 AuthService

**文件**：`src/main/java/cn/eta/team/eta/auth/AuthService.java`

认证服务，跨默认库和用户库操作。

**关键约束：不能使用 `@Transactional`**

`@Transactional` 在方法开始时就从 `DataSource` 获取 Connection 并绑定到线程。此时 `TenantContext` 的值决定了路由到哪个库。方法内后续切换 `TenantContext` 不会改变已绑定的 Connection。

AuthService 的方法需要在默认库（查 UserAccount）和用户库（查 UserProfile）之间切换，所以**全部去掉 `@Transactional`**，让每个 Repository 调用独立获取 Connection。

**各方法的数据源路由**：

| 方法 | UserAccount（默认库） | UserProfile（用户库） | 处理方式 |
|---|---|---|---|
| `register` | save | save | 先存默认库 → 建用户库 → `TenantContext.set` → 存用户库 → `clear` |
| `login` | findByEmail | findById | 默认库查账号 → `ensureDatabase` → `set` → 查档案 → `clear` |
| `me` | findById | findById | `runAsDefault` 查账号 → 用户库查档案（Filter 已设 TenantContext） |
| `updateProfile` | findById | save | 用户库更新档案 → `runAsDefault` 查账号组装返回 |
| `changePassword` | save | — | 全部 `runAsDefault`，只操作默认库 |

---

## 四、完整请求生命周期

以 `GET /api/v1/tasks`（获取任务列表）为例：

```
1. 前端发送请求，Header: Authorization: Bearer <jwt>
2. Tomcat 从线程池取一个线程 Thread-3
3. JwtAuthenticationFilter:
   - 解析 JWT → userId = "abc-123"
   - ensureRegistered(userId) → 路由表中有则跳过，没有则建库
   - SecurityContext 设置认证信息
   - TenantContext.set("abc-123")  ← Thread-3 的 ThreadLocal = "abc-123"
4. chain.doFilter() → TaskController.list()
5. TaskService.list() → taskRepository.findWithFilters(...)
6. Hibernate 调用 DataSource.getConnection()
7. routingDataSource.determineTargetDataSource():
   - determineCurrentLookupKey() → TenantContext.get() → "abc-123"
   - resolvedDataSources.get("abc-123") → 用户的 HikariDataSource
   - 返回该 DataSource 的 Connection
8. SQL 在 ./data/users/abc-123.mv.db 上执行
9. 返回结果 → Controller → 前端
10. finally: TenantContext.clear()  ← Thread-3 的 ThreadLocal 清除
11. Thread-3 归还线程池
```

---

## 五、关键设计决策

### 5.1 每用户 H2 文件 vs Schema 隔离

| 维度 | 每用户 H2 文件 | Schema 隔离 |
|---|---|---|
| 数据边界 | 文件级，天然清晰 | 表级，需严格管理 schema |
| 云端同步 | 传输整个文件 | 需逐表导出/导入 |
| 连接池 | 每用户独立 HikariCP | 共享连接池，切换 schema |
| 用户切换开销 | 新建 HikariDataSource（一次性） | 无额外开销 |
| 适用场景 | 本地应用、用户少、切换少 | 云端 SaaS、用户多、切换频繁 |

本项目是本地桌面应用后端，用户少、切换少、需要离线运行、后续要做云端同步，因此选择每用户 H2 文件。

### 5.2 UserAccount 与 UserProfile 分离

- **UserAccount**（默认库）：登录凭证，认证时还不知道用户属于哪个库，必须在默认库
- **UserProfile**（用户库）：个人档案，属于业务数据，随用户库同步
- 分离后 `updateProfile` 只操作用户库，天然路由正确，不需要特殊处理

### 5.3 @Transactional 的使用规则

| 场景 | 能否用 @Transactional |
|---|---|
| 方法全程在同一个库内操作（业务 Service） | 可以 |
| 方法内需要切换 TenantContext（AuthService） | **不能** |

原因：`@Transactional` 在方法开始时就绑定 Connection，后续切换 `TenantContext` 无效。

### 5.4 默认库不用 Hibernate ddl-auto

改用手动 `schema.sql` + `DataInitializer` 执行，避免 Hibernate 在默认库中创建多余的业务表（`eta_task`、`eta_note` 等），保持默认库只有 `eta_user_account`。

---

## 六、使用方法与实例

### 6.1 新增业务实体

以新增一个"学习计划"（StudyPlan）实体为例：

**步骤 1：创建实体类**

```java
// src/main/java/cn/eta/team/eta/domain/study/StudyPlan.java
@Entity
@Table(name = "eta_study_plan")
@Getter
@Setter
public class StudyPlan {
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
```

**步骤 2：在 schema-user.sql 中加建表语句**

```sql
-- 学习计划
CREATE TABLE IF NOT EXISTS eta_study_plan (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description CLOB,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

> 已存在的用户库会在下次登录时通过 `ensureDatabase` 重新执行脚本，`IF NOT EXISTS` 会自动补齐新表。

**步骤 3：创建 Repository**

```java
public interface StudyPlanRepository extends JpaRepository<StudyPlan, String> {
    Page<StudyPlan> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
```

**步骤 4：创建 Service（全程在用户库，可用 @Transactional）**

```java
@Service
@RequiredArgsConstructor
public class StudyPlanService {
    private final StudyPlanRepository studyPlanRepository;

    @Transactional(readOnly = true)
    public Paged<StudyPlan> list(QueryRequest q) {
        Pageable pageable = PageRequest.of(q.page() - 1, q.pageSize());
        return Paged.of(studyPlanRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @Transactional
    public StudyPlan create(CreateRequest req) {
        StudyPlan plan = new StudyPlan();
        plan.setTitle(req.title());
        plan.setDescription(req.description());
        plan.setCreatedAt(Instant.now());
        return studyPlanRepository.save(plan);
    }
}
```

> 业务 Service 不需要关心租户切换——Filter 已经设置了 `TenantContext`，所有 Repository 调用自动路由到当前用户库。

**步骤 5：创建 Controller**

```java
@RestController
@RequestMapping("/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {
    private final StudyPlanService studyPlanService;

    @GetMapping
    public Result<Paged<StudyPlan>> list(@ModelAttribute QueryRequest q) {
        return Result.ok(studyPlanService.list(q));
    }

    @PostMapping
    public Result<StudyPlan> create(@Valid @RequestBody CreateRequest req) {
        return Result.ok(studyPlanService.create(req));
    }
}
```

### 6.2 在业务代码中操作默认库

如果某个业务 Service 需要访问默认库的 `eta_user_account`（比如查询某个用户的邮箱），使用 `TenantContext.runAsDefault()`：

```java
@Service
@RequiredArgsConstructor
public class SomeService {
    private final UserAccountRepository userAccountRepository;
    private final TaskRepository taskRepository;

    public void doSomething(String userId) {
        // 操作默认库：查询用户账号信息
        UserAccount account = TenantContext.runAsDefault(() ->
            userAccountRepository.findById(userId).orElseThrow()
        );

        // 操作用户库：查询任务（TenantContext 已由 Filter 设置，自动路由）
        List<Task> tasks = taskRepository.findAll();

        // ...
    }
}
```

`runAsDefault` 会临时清除 `TenantContext`，使数据库操作路由到默认库，执行完后自动恢复原上下文。

### 6.3 在非请求线程中操作数据库

定时任务、异步线程等不经过 Filter 的场景，需要手动设置 `TenantContext`：

```java
@Component
@RequiredArgsConstructor
public class CloudSyncScheduler {
    private final UserDatabaseInitializer userDatabaseInitializer;
    private final TaskRepository taskRepository;

    @Scheduled(fixedRate = 300000)  // 每 5 分钟同步一次
    public void syncUserData(String userId) {
        userDatabaseInitializer.ensureRegistered(userId);  // 确保用户库已注册
        TenantContext.set(userId);
        try {
            List<Task> tasks = taskRepository.findAll();
            // 上传到云端...
        } finally {
            TenantContext.clear();  // 必须清除
        }
    }
}
```

### 6.4 H2 Console 中查看不同用户的数据库

在 H2 Console 登录页面，通过不同的 JDBC URL 连接不同的库：

**默认库（用户登录凭证）：**
```
jdbc:h2:file:D:/Code/whyJava/eta-local-server/data/eta;MODE=MySQL;AUTO_SERVER=TRUE
```

**某个用户的业务库：**
```
jdbc:h2:file:D:/Code/whyJava/eta-local-server/data/users/{userId};MODE=MySQL;AUTO_SERVER=TRUE
```

- 用户名：`sa`，密码：（空）
- `AUTO_SERVER=TRUE` 必须带上，否则应用运行时 Console 无法同时打开文件
- 路径末尾不加 `.mv.db` 扩展名

---

## 七、配置项

`application.yml` 中的相关配置：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/eta;MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: none          # 关闭自动建表，默认库用 schema.sql，用户库用 schema-user.sql
    open-in-view: false
  sql:
    init:
      mode: always             # 启用 SQL 初始化（实际由 DataInitializer 手动执行）

eta:
  user-db:
    path: ./data/users         # 用户数据库文件存放目录
  jwt:
    secret: <your-secret-key>
    expiration-ms: 86400000   # 24 小时
```