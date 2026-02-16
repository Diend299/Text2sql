package org.example.texttosql.llm;

public class LlmPromptBuilder {
    public static String buildPrompt(String question, String relevantSchemaStr, String relevantKnowledgeStr, String fewShotExamplesStr) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("### Instructions:\n");
        prompt.append("Given the following database schema, relevant common knowledge (if any), and a user's question, your task is to generate a valid PostgreSQL-compatible SQL query for StarRocks database.\n");
        prompt.append("**Crucially, you MUST ONLY use the EXACT table names and column names that are explicitly provided in the database schema context below. DO NOT invent or assume any other names.**\n");
        prompt.append("**Before generating the SQL query, carefully analyze the provided tables and their columns. If the required columns for the question span across multiple tables, or if conditions need to reference columns in different tables, you MUST perform appropriate JOIN operations.**\n");
        prompt.append("**Identify the common columns (e.g., user IDs, game IDs, dates) between tables to form correct JOIN conditions.**\n");
        prompt.append("**Be extremely cautious with similar column names (e.g., 'suserid' vs 'uid') across different tables. Always verify the correct column name from the SPECIFIC table context provided for the current question and ensure consistency in JOIN conditions.**\n");
        prompt.append("Ensure the SQL query is syntactically correct and semantically accurate for the given question and schema.\n");
        prompt.append("The query should not include any comments or explanations, just the SQL itself.\n");
        prompt.append("Do not wrap the SQL query in markdown code blocks or any other special formatting. Directly output the SQL query.\n");
        prompt.append("Pay close attention to column types and table relationships.\n");

        if (relevantKnowledgeStr != null && !relevantKnowledgeStr.isEmpty()) {
            prompt.append("\n### Relevant Common Knowledge:\n").append(relevantKnowledgeStr);
        }

        prompt.append("\n\n### Database Schema:\n").append(relevantSchemaStr);

        if (fewShotExamplesStr != null && !fewShotExamplesStr.isEmpty()) {
            prompt.append("\n\n").append(fewShotExamplesStr);
        }

        prompt.append("\n\n### Question:\n").append(question);
        prompt.append("\n\n### Thought Process:\n");
        prompt.append("1. Identify all required columns from the question: [list them]\n");
        prompt.append("2. Identify all tables that contain these columns from the provided schema: [list them]\n");
        prompt.append("3. Determine if JOINs are necessary. If yes, identify common columns for joining: [explain join logic]\n");
        prompt.append("4. Formulate the SQL query based on the above analysis.\n");
        prompt.append("\n### SQL Query:\n");

        return prompt.toString();
    }
}