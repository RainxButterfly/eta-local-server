// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common.util;

import java.util.Locale;

/**
 * 文件工具：扩展名、安全化文件名、体积可读格式化。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class FileUtils {

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"};

    private FileUtils() {
    }

    /** 返回小写扩展名（不含点），无扩展名返回空串。 */
    public static String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (dot < 0 || dot < slash) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 判断是否为图片扩展名。 */
    public static boolean isImage(String filename) {
        String ext = extension(filename);
        for (String e : IMAGE_EXTENSIONS) {
            if (e.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /** 去除路径分隔与非法字符，返回安全的文件名（防路径注入）。 */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        String name = filename.replace('\\', '/');
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }
        return name.replaceAll("[\\\\/:*?\"<>|\\u0000-\\u001f]", "_").trim();
    }

    /** 字节数转可读体积：B / KB / MB / GB / TB。 */
    public static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int i = -1;
        while (value >= 1024 && i < units.length - 1) {
            value /= 1024;
            i++;
        }
        return String.format("%.1f %s", value, units[i]);
    }
}