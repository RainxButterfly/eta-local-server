// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.config;

import cn.eta.team.eta.auth.User;
import cn.eta.team.eta.auth.UserRepository;
import cn.eta.team.eta.task.Task;
import cn.eta.team.eta.task.TaskCategory;
import cn.eta.team.eta.task.TaskCategoryRepository;
import cn.eta.team.eta.task.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 初始化演示数据：首次启动时写入一个演示账号与示例任务，便于直接联调。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TaskRepository taskRepository,
                           TaskCategoryRepository categoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        Instant now = Instant.now();

        User user = new User();
        user.setEmail("demo@eta.cn");
        user.setNickname("Roxy");
        user.setBio("测试简介内容");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        userRepository.save(user);
        String owner = user.getId();

        List<CategorySeed> categories = List.of(
                new CategorySeed("课程作业", "#3d7bd6"),
                new CategorySeed("考试准备", "#2ba9c9"),
                new CategorySeed("个人成长", "#7c6cf0"),
                new CategorySeed("复习计划", "#2fa47c"));
        String[] catIds = new String[4];
        for (int i = 0; i < categories.size(); i++) {
            TaskCategory c = new TaskCategory();
            c.setOwnerId(owner);
            c.setName(categories.get(i).name());
            c.setColor(categories.get(i).color());
            categoryRepository.save(c);
            catIds[i] = c.getId();
        }

        saveTask(owner, catIds[0], "完成高数第三章习题", "数学", "violet", "今日 18:00", "doing", 68, "高");
        saveTask(owner, catIds[1], "准备英语六级听力材料", "英语", "amber", "明日 09:00", "todo", 0, "中");
        saveTask(owner, catIds[2], "更新个人简历与作品集", "求职", "green", "周五 23:59", "doing", 32, "中");
        saveTask(owner, catIds[3], "复习操作系统——进程调度", "计算机", "cyan", "周六 20:00", "todo", 0, "低");
        log.info("[ETA] 演示数据初始化完成。演示账号 demo@eta.cn / 123456");
    }

    private void saveTask(String owner, String categoryId, String title, String tag,
                          String tone, String due, String status, int progress, String priority) {
        Task task = new Task();
        task.setOwnerId(owner);
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