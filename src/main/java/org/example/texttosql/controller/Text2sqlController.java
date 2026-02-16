package org.example.texttosql.controller;

import org.example.texttosql.service.Text2sqlService;
import org.example.texttosql.model.QuestionRequest;
import org.example.texttosql.model.SqlExecutionResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/text2sql")
public class Text2sqlController {

    private final Text2sqlService text2sqlService;

    public Text2sqlController(Text2sqlService text2sqlService) {
        this.text2sqlService = text2sqlService;
    }

    @PostMapping("/query")
    public SqlExecutionResult query(@RequestBody QuestionRequest request) {
        return text2sqlService.processQuestion(request);
    }
}