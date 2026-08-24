package cn.eta.team.eta.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储工具类（本地磁盘存储）
 *
 * @author ormisnal
 * @since 2026-08-24
 */
public final class FileStorageUtils {

    private static String STORAGE_ROOT_PATH = "./data/files";


    private FileStorageUtils() {}

    /**
     * 设置存储根目录（在应用启动时调用一次即可）
     * @param rootPath 存储根目录，如 "/var/data/files"
     */
    public static void setStorageRootPath(String rootPath) {
        if (rootPath != null && !rootPath.isBlank()) {
            STORAGE_ROOT_PATH = rootPath;
        }
    }

    /**
     * 保存文件到本地
     * @param content 文件字节数组
     * @param fileName 文件名
     * @return 文件相对路径
     * @throws RuntimeException 如果保存失败
     */
    public static String saveToLocal(byte[] content, String fileName) {

        Path targetDir = Paths.get(STORAGE_ROOT_PATH);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建文件目录失败: " + targetDir, e);
        }

        Path targetPath = targetDir.resolve(fileName);

        try {
            Files.write(targetPath, content);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败: " + targetPath, e);
        }

        return targetDir + "\\" + fileName;
    }

    /**
     * 读取文件内容
     * @param relativePath
     * @return 文件字节数组
     * @throws RuntimeException 如果文件不存在或读取失败
     */
    public static byte[] readFromLocal(String relativePath) {
        Path filePath = buildSafePath(relativePath);
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + relativePath, e);
        }
    }

    /**
     * 删除文件
     * @param relativePath 相对路径
     * @return true 删除成功，false 文件不存在
     */
    public static boolean deleteFromLocal(String relativePath) {
        Path filePath = buildSafePath(relativePath);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败: " + relativePath, e);
        }
    }

    /**
     * 检查文件是否存在
     * @param relativePath 相对路径
     * @return true 存在，false 不存在
     */
    public static boolean exists(String relativePath) {
        Path filePath = buildSafePath(relativePath);
        return Files.exists(filePath) && !Files.isDirectory(filePath);
    }

    /**
     * 构建安全的文件路径（防止路径穿越攻击）
     * @param relativePath 相对路径
     * @return 规范化的 Path 对象
     * @throws IllegalArgumentException 如果路径非法
     */
    private static Path buildSafePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 拼接完整路径
        Path fullPath = Paths.get(STORAGE_ROOT_PATH).resolve(relativePath);
        Path normalized = fullPath.normalize();

        if (!normalized.startsWith(Paths.get(STORAGE_ROOT_PATH).normalize())) {
            throw new IllegalArgumentException("非法文件路径: " + relativePath);
        }

        return normalized;
    }
}