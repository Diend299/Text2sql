package org.example.texttosql.model;

import java.util.List;

// 用于接收前端请求的 DTO
public class QuestionRequest {
    private String question;
    private List<String> tableList;
    private String knowledge;

    public QuestionRequest() {}

    public QuestionRequest(String question, List<String> tableList, String knowledge) {
        this.question = question;
        this.tableList = tableList;
        this.knowledge = knowledge;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getTableList() {
        return tableList;
    }

    public void setTableList(List<String> tableList) {
        this.tableList = tableList;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(String knowledge) {
        this.knowledge = knowledge;
    }
}