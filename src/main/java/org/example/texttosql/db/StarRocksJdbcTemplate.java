package org.example.texttosql.db;

import org.example.texttosql.model.SqlExecutionResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StarRocksJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public StarRocksJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlExecutionResult executeQuery(String sqlId, String question, String sql) {
        try {
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                // 应用 normalize_numbers_in_result 逻辑
                List<Map<String, Object>> normalizedRows = normalizeNumbersInResult(rows);
                return new SqlExecutionResult(sqlId, question, "success", null, sql, normalizedRows);
            } else {
                int rowsAffected = jdbcTemplate.update(sql);
                return new SqlExecutionResult(sqlId, question, "success", "Rows affected: " + rowsAffected, sql, null);
            }
        } catch (Exception e) {
            return new SqlExecutionResult(sqlId, question, "error", e.getMessage(), sql, null);
        }
    }

    // 移植 Python 中的 normalize_numbers_in_result 逻辑
    private List<Map<String, Object>> normalizeNumbersInResult(List<Map<String, Object>> resultList) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            Map<String, Object> normalizedRow = new HashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Double) {
                    Double doubleValue = (Double) value;
                    if (doubleValue == Math.floor(doubleValue)) { // 如果是浮点数但无小数部分
                        normalizedRow.put(entry.getKey(), doubleValue.intValue());
                    } else { // 保留两位小数
                        normalizedRow.put(entry.getKey(), BigDecimal.valueOf(doubleValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    }
                } else if (value instanceof BigDecimal) {
                    // BigDecimal也保留两位小数
                    normalizedRow.put(entry.getKey(), ((BigDecimal) value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                } else {
                    normalizedRow.put(entry.getKey(), value);
                }
            }
            normalized.add(normalizedRow);
        }
        return normalized;
    }
}