// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * JSON 工具（基于 Jackson 3，包名 tools.jackson）。
 * <p>
 * 提供对象 &lt;-&gt; JSON 字符串的便捷转换；方法抛出的 {@link tools.jackson.core.JacksonException} 为运行时异常。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private JsonUtils() {
    }

    /** 对象序列化为 JSON 字符串；null 输入返回 null。 */
    public static String toJson(Object obj) {
        return obj == null ? null : MAPPER.writeValueAsString(obj);
    }

    /** 对象序列化为带缩进的 JSON 字符串；null 输入返回 null。 */
    public static String toJsonPretty(Object obj) {
        return obj == null ? null : MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /** 反序列化为指定类型；空字符串返回 null。 */
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return MAPPER.readValue(json, type);
    }

    /** 反序列化为泛型类型（List/Map 等）；空字符串返回 null。 */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return MAPPER.readValue(json, type);
    }

    /** 反序列化为 Map。 */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    /** 反序列化为 List&lt;Map&gt;，便于遍历 JSON 数组。 */
    public static List<Map<String, Object>> toList(String json) {
        return fromJson(json, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    /** 安全反序列化：解析失败返回 null 而非抛异常（适合容错解析）。 */
    public static <T> T tryFromJson(String json, Class<T> type) {
        try {
            return fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }
}