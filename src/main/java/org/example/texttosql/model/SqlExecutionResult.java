package org.example.texttosql.model;

import java.util.List;
import java.util.Map;

public class SqlExecutionResult {
    private String sqlId;
    private String question;
    private String status; // success, error, llm_error, workflow_error
    private String errorMessage;
    private String generatedSql;
    private List<Map<String, Object>> data; // 对于 SELECT 结果
    // Constructors, getters, setters
}
