package cn.eta.team.eta.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具：ISO 时间戳、格式化、解析与相对时间展示。
 */
public final class TimeUtils {

    public static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TimeUtils() {
    }

    /** 当前 UTC 时间戳（ISO-8601）。 */
    public static Instant now() {
        return Instant.now();
    }

    /** 当前时间的字符串化 ISO-8601。 */
    public static String nowIso() {
        return Instant.now().toString();
    }

    /** LocalDateTime 转 ISO-8601 字符串。 */
    public static String toIso(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atOffset(ZoneOffset.UTC).toInstant().toString();
    }

    /** 格式化为 yyyy-MM-dd HH:mm:ss。 */
    public static String format(LocalDateTime ldt) {
        return ldt == null ? null : DEFAULT.format(ldt);
    }

    /** 解析已格式化的字符串。 */
    public static LocalDateTime parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(text, DEFAULT);
    }

    /** UTC Instant 转本地时间的格式化字符串。 */
    public static String formatInstant(Instant instant) {
        return instant == null ? null
                : DEFAULT.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    /**
     * 相对时间：刚刚 / n 分钟前 / n 小时前 / n 天前 / 具体日期。
     */
    public static String relative(Instant when) {
        if (when == null) {
            return "";
        }
        long sec = Math.max(0, Instant.now().getEpochSecond() - when.getEpochSecond()) + 1;
        if (sec < 60) {
            return "刚刚";
        }
        long min = sec / 60;
        if (min < 60) {
            return min + " 分钟前";
        }
        long hour = min / 60;
        if (hour < 24) {
            return hour + " 小时前";
        }
        long day = hour / 24;
        if (day < 7) {
            return day + " 天前";
        }
        return DATE.format(LocalDate.ofInstant(when, ZoneId.systemDefault()));
    }

    /** 判断给定时间是否为今天。 */
    public static boolean isToday(Instant when) {
        return when != null
                && LocalDate.ofInstant(when, ZoneId.systemDefault()).equals(LocalDate.now());
    }
}