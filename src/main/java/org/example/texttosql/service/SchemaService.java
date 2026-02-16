package org.example.texttosql.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private final Map<String, String> tableSchemasMap = new HashMap<>();
    private String fewShotExamplesStr = "";
    private final ResourceLoader resourceLoader;

    public SchemaService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        loadSchemaAndExamples();
    }

    public Map<String, String> getTableSchemasMap() {
        return tableSchemasMap;
    }

    public String getFewShotExamplesStr() {
        return fewShotExamplesStr;
    }

    private void loadSchemaAndExamples() {
        // 加载 create_table.sql
        try {
            Resource resource = resourceLoader.getResource("classpath:data/create_table.sql");
            String fullSqlContent = resource.getContentAsString(StandardCharsets.UTF_8);

            // 简单解析 CREATE TABLE 语句，与 Python 版本类似
            Pattern pattern = Pattern.compile("CREATE TABLE (IF NOT EXISTS\\s+)?`?(\\w+)`?[\\s\\S]*?;", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fullSqlContent);
            while (matcher.find()) {
                String fullCreateStmt = matcher.group(0);
                String tableName = matcher.group(2).toLowerCase();
                tableSchemasMap.put(tableName, fullCreateStmt);
            }
            System.out.println("SchemaService: Loaded " + tableSchemasMap.size() + " tables from create_table.sql");

        } catch (IOException e) {
            System.err.println("SchemaService: Failed to load create_table.sql: " + e.getMessage());
        }

        // 加载 goldensql.json (few-shot examples)
        try {
            Resource resource = resourceLoader.getResource("classpath:data/goldensql.json");
            String jsonContent = resource.getContentAsString(StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> allGoldensqlData = objectMapper.readValue(jsonContent, List.class);

            // 选取前3个作为 few-shot
            fewShotExamplesStr = allGoldensqlData.stream()
                    .limit(3)
                    .map(example -> "### Question:\n" + example.getOrDefault("question", "N/A") + "\n### SQL Query:\n" + example.getOrDefault("sql", "N/A"))
                    .collect(Collectors.joining("\n\n"));
            System.out.println("SchemaService: Loaded few-shot examples from goldensql.json");

        } catch (IOException e) {
            System.err.println("SchemaService: Failed to load goldensql.json: " + e.getMessage());
        }
    }
}