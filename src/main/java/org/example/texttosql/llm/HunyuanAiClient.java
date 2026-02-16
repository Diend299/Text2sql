package org.example.texttosql.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class HunyuanAiClient {

    private final String endpoint; // 完整的 HTTP API URL（包含路径），例如 https://hunyuan.tencentcloudapi.com/v1/chat/completions
    private final String model;
    private final String bearerToken; // 可选：若使用 Bearer token 验证
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HunyuanAiClient(@Value("${tencent.hunyuan.endpoint:}") String endpoint,
                           @Value("${tencent.hunyuan.model:}") String model,
                           @Value("${tencent.hunyuan.bearerToken:}") String bearerToken) {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("tencent.hunyuan.endpoint is required and must be a full URL to the HTTP API");
        }
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("tencent.hunyuan.model is required");
        }
        this.endpoint = endpoint;
        this.model = model;
        this.bearerToken = (bearerToken != null && !bearerToken.isBlank()) ? bearerToken : null;
        System.out.println("Hunyuan HTTP client initialized (endpoint=" + endpoint + ", model=" + model + ")");
    }

    public String generateSql(String promptContent) {
        try {
            // 构建请求体：兼容常见 Chat Completions 接口
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode messageObj = objectMapper.createObjectNode();
            messageObj.put("role", "user");
            messageObj.put("content", promptContent);
            messages.add(messageObj);
            
            root.set("messages", messages);
            root.put("temperature", 0.0);
            
            String requestJson = objectMapper.writeValueAsString(root);

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if (bearerToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }

            byte[] out = requestJson.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(out.length);
            conn.connect();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            int status = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder respSb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) respSb.append(line).append('\n');

            String respStr = respSb.toString();
            JsonNode respRoot = objectMapper.readTree(respStr);

            // 尝试从常见字段提取回答
            if (respRoot.has("choices") && respRoot.get("choices").isArray() && respRoot.get("choices").size() > 0) {
                JsonNode first = respRoot.get("choices").get(0);
                if (first.has("message") && first.get("message").has("content")) {
                    return first.get("message").get("content").asText();
                }
                if (first.has("text")) {
                    return first.get("text").asText();
                }
            }

            // 回退到 raw 输出
            return respStr;

        } catch (Exception e) {
            throw new RuntimeException("Error calling Hunyuan HTTP API: " + e.getMessage(), e);
        }
    }
}
