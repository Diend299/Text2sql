package org.example.texttosql.util;

public class SqlSanitizer {
    public static String sanitizeSql(String rawSql) {
        if (rawSql == null) return "";
        String sanitized = rawSql.trim();
        if (sanitized.startsWith("```sql")) {
            sanitized = sanitized.substring("```sql".length()).trim();
        }
        if (sanitized.endsWith("```")) {
            sanitized = sanitized.substring(0, sanitized.length() - "```".length()).trim();
        }
        // 这里可以添加更多编码清理逻辑，例如移除不可打印字符
        return sanitized;
    }
}