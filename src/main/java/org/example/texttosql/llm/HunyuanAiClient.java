package org.example.texttosql.llm;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;
import com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsRequest;
import com.tencentcloudapi.hunyuan.v20230901.models.Message;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HunyuanAiClient {

    private final HunyuanClient client;
    private final String model;

    public HunyuanAiClient(@Value("${tencent.hunyuan.secretId}") String secretId,
                           @Value("${tencent.hunyuan.secretKey}") String secretKey,
                           @Value("${tencent.hunyuan.endpoint}") String endpoint,
                           @Value("${tencent.hunyuan.model}") String model) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(endpoint);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            this.client = new HunyuanClient(cred, "", clientProfile); // Region 可以为空
            this.model = model;
            System.out.println("Hunyuan AI client initialized successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Hunyuan AI client", e);
        }
    }

    public String generateSql(String promptContent) {
        try {
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            req.setMessages(new Message[]{new Message("user", promptContent)});
            req.setModel(this.model);
            req.setTemperature(0.0);

            com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsResponse resp = client.ChatCompletions(req);

            if (resp != null && resp.getChoices() != null && resp.getChoices().length > 0) {
                return resp.getChoices()[0].getMessage().getContent();
            }
            return "Error: No content found in Hunyuan AI response.";

        } catch (Exception e) {
            throw new RuntimeException("Error calling Tencent Hunyuan AI API", e);
        }
    }
}
