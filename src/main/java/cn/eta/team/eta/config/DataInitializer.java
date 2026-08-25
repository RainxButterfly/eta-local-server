// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.config;

import cn.eta.team.eta.auth.UserAccount;
import cn.eta.team.eta.auth.UserAccountRepository;
import cn.eta.team.eta.auth.UserProfile;
import cn.eta.team.eta.auth.UserProfileRepository;
import cn.eta.team.eta.domain.task.Task;
import cn.eta.team.eta.domain.task.TaskCategory;
import cn.eta.team.eta.domain.task.TaskCategoryRepository;
import cn.eta.team.eta.domain.task.TaskRepository;
import cn.eta.team.eta.tenant.TenantContext;
import cn.eta.team.eta.tenant.UserDatabaseInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDatabaseInitializer userDatabaseInitializer;
    private final DataSource defaultDataSource;

    public DataInitializer(UserAccountRepository userAccountRepository,
                           UserProfileRepository userProfileRepository,
                           TaskRepository taskRepository,
                           TaskCategoryRepository categoryRepository,
                           PasswordEncoder passwordEncoder,
                           UserDatabaseInitializer userDatabaseInitializer,
                           DataSource defaultDataSource) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDatabaseInitializer = userDatabaseInitializer;
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = defaultDataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema.sql"));
        } catch (Exception e) {
            throw new RuntimeException("初始化默认数据库失败", e);
        }

        if (userAccountRepository.count() > 0) {
            return;
        }

        UserAccount account = new UserAccount();
        account.setEmail("demo@eta.cn");
        account.setPasswordHash(passwordEncoder.encode("123456"));
        userAccountRepository.save(account);
        String userId = account.getId();

        userDatabaseInitializer.createDatabase(userId);

        UserProfile profile = new UserProfile();
        profile.setId(userId);
        profile.setNickname("Roxy");
        profile.setBio("测试简介内容");

        TenantContext.set(userId);
        try {
            userProfileRepository.save(profile);

            List<CategorySeed> categories = List.of(
                    new CategorySeed("课程作业", "#3d7bd6"),
                    new CategorySeed("考试准备", "#2ba9c9"),
                    new CategorySeed("个人成长", "#7c6cf0"),
                    new CategorySeed("复习计划", "#2fa47c"));
            String[] catIds = new String[4];
            for (int i = 0; i < categories.size(); i++) {
                TaskCategory c = new TaskCategory();
                c.setName(categories.get(i).name());
                c.setColor(categories.get(i).color());
                categoryRepository.save(c);
                catIds[i] = c.getId();
            }

            saveTask(catIds[0], "完成高数第三章习题", "数学", "violet", "今日 18:00", "doing", 68, "高");
            saveTask(catIds[1], "准备英语六级听力材料", "英语", "amber", "明日 09:00", "todo", 0, "中");
            saveTask(catIds[2], "更新个人简历与作品集", "求职", "green", "周五 23:59", "doing", 32, "中");
            saveTask(catIds[3], "复习操作系统——进程调度", "计算机", "cyan", "周六 20:00", "todo", 0, "低");
            log.info("[ETA] 演示数据初始化完成。演示账号 demo@eta.cn / 123456");
        } finally {
            TenantContext.clear();
        }
    }

    private void saveTask(String categoryId, String title, String tag,
                          String tone, String due, String status, int progress, String priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setCategoryId(categoryId);
        task.setCategory(categoryName(categoryId));
        task.setTag(tag);
        task.setTone(tone);
        task.setDue(due);
        task.setStatus(status);
        task.setProgress(progress);
        task.setPriority(priority);
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        taskRepository.save(task);
    }

    private String categoryName(String categoryId) {
        return categoryRepository.findById(categoryId).map(TaskCategory::getName).orElse("默认分类");
    }

    private record CategorySeed(String name, String color) {
    }
}
