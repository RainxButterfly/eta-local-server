// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LocalCloudStorageProvider implements CloudStorageProvider {

    private final Path baseDir;

    public LocalCloudStorageProvider(@Value("${eta.sync.cloud-dir:./data/cloud-sync}") String cloudDir) {
        this.baseDir = Paths.get(cloudDir);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("创建云端模拟目录失败: " + baseDir, e);
        }
    }

    @Override
    public void upload(String key, byte[] data) throws IOException {
        Path target = baseDir.resolve(key);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
    }

    @Override
    public byte[] download(String key) throws IOException {
        return Files.readAllBytes(baseDir.resolve(key));
    }

    @Override
    public boolean exists(String key) throws IOException {
        return Files.exists(baseDir.resolve(key));
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(baseDir.resolve(key));
    }

    @Override
    public List<String> list(String prefix) throws IOException {
        Path dir = baseDir.resolve(prefix);
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> baseDir.relativize(p).toString().replace('\\', '/'))
                    .toList();
        }
    }
}
