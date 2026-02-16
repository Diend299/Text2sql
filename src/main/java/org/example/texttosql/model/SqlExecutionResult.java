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

    public SqlExecutionResult() {}

    public SqlExecutionResult(String sqlId, String question, String status, String errorMessage, String generatedSql, List<Map<String, Object>> data) {
        this.sqlId = sqlId;
        this.question = question;
        this.status = status;
        this.errorMessage = errorMessage;
        this.generatedSql = generatedSql;
        this.data = data;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getGeneratedSql() {
        return generatedSql;
    }

    public void setGeneratedSql(String generatedSql) {
        this.generatedSql = generatedSql;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}