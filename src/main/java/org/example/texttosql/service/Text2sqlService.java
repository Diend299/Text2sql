package org.example.texttosql.service;

import org.example.texttosql.llm.HunyuanAiClient;
import org.example.texttosql.db.StarRocksJdbcTemplate;
import org.example.texttosql.model.QuestionRequest;
import org.example.texttosql.model.SqlExecutionResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Text2sqlService {

    private final HunyuanAiClient hunyuanAiClient;
    private final StarRocksJdbcTemplate starRocksJdbcTemplate;
    private final SchemaService schemaService; // 用于获取加载好的schema和few-shot

    public Text2sqlService(HunyuanAiClient hunyuanAiClient,
                           StarRocksJdbcTemplate starRocksJdbcTemplate,
                           SchemaService schemaService) {
        this.hunyuanAiClient = hunyuanAiClient;
        this.starRocksJdbcTemplate = starRocksJdbcTemplate;
        this.schemaService = schemaService;
    }

    public SqlExecutionResult processQuestion(QuestionRequest request) {
        String question = request.getQuestion();
        List<String> providedTableList = request.getTableList();
        String providedKnowledge = request.getKnowledge();

        // 1. 获取相关Schema (基于 providedTableList)
        StringBuilder relevantSchemaBuilder = new StringBuilder();
        Set<String> allTableNamesLower = schemaService.getTableSchemasMap().keySet();

        if (providedTableList != null && !providedTableList.isEmpty()) {
            for (String tableName : providedTableList) {
                String tableNameLower = tableName.toLowerCase();
                if (allTableNamesLower.contains(tableNameLower)) {
                    relevantSchemaBuilder.append(schemaService.getTableSchemasMap().get(tableNameLower)).append("\n\n");
                } else {
                    System.err.println("Text2sqlService: Warning: Table '" + tableName + "' from request's tableList not found in parsed schema map.");
                }
            }
        }
        String relevantSchemaStr = relevantSchemaBuilder.toString().trim();
        if (relevantSchemaStr.isEmpty()) {
            System.err.println("Text2sqlService: No specific schema found from provided tableList. Using full schema as fallback.");
            // Fallback: 如果提供的table_list中的表都没找到，提供所有Schema (如果Schema不大)
            relevantSchemaStr = String.join("\n\n", schemaService.getTableSchemasMap().values());
        }

        // 2. 获取Few-Shot Examples
        String fewShotExamplesStr = schemaService.getFewShotExamplesStr();

        // 3. 构建Prompt
        String promptContent = LlmPromptBuilder.buildPrompt(
                question, relevantSchemaStr, providedKnowledge, fewShotExamplesStr);

        // 4. 调用LLM生成SQL
        String rawLlmResponse;
        try {
            rawLlmResponse = hunyuanAiClient.generateSql(promptContent);
        } catch (RuntimeException e) {
            return new SqlExecutionResult(null, question, "llm_error", "LLM API call failed: " + e.getMessage(), null, null);
        }

        // 5. 清理SQL
        String generatedSql = SqlSanitizer.sanitizeSql(rawLlmResponse);

        // 6. 执行SQL
        // 由于 request 中没有 sqlId，这里可以生成一个或使用默认值
        String sqlId = "generated_" + System.currentTimeMillis();
        return starRocksJdbcTemplate.executeQuery(sqlId, question, generatedSql);
    }
}