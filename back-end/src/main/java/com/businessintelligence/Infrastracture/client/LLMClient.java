package com.businessintelligence.Infrastracture.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LLMClient {

    private final RestTemplate restTemplate;
    private final String llmApiUrl;
    private final String apiKey;
    private final String model;

    public LLMClient(RestTemplate restTemplate,
                     @Value("${llm.api.url}") String llmApiUrl,
                     @Value("${llm.api.key}") String apiKey,
                     @Value("${llm.api.model:deepseek-chat}") String model) {
        this.restTemplate = restTemplate;
        this.llmApiUrl = llmApiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String invoke(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建符合Chat API格式的请求体
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);

        // 设置消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统消息（可选）
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个智能助手，可以根据用户需求生成SQL查询语句。" +
                "请只返回SQL语句，不要包含其他解释。");
        messages.add(systemMessage);

        // 添加用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 添加生成参数
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 200);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 打印请求信息用于调试
        System.out.println("LLM请求URL: " + llmApiUrl);
        System.out.println("LLM请求体: " + requestBody);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
                llmApiUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            System.out.println("LLM响应: " + response.getBody());
            return parseLLMResponse(response.getBody());
        } else {
            System.err.println("LLM API调用失败，状态码: " + response.getStatusCode());
            System.err.println("LLM API错误响应: " + response.getBody());
            throw new RuntimeException("LLM API调用失败: " + response.getStatusCode());
        }
    }

    private String parseLLMResponse(String responseBody) {
        try {
            // 简单JSON解析，提取content字段
            int contentStart = responseBody.indexOf("\"content\":\"") + 12;
            if (contentStart < 12) {
                // 尝试其他可能的格式
                contentStart = responseBody.indexOf("\"text\":\"") + 8;
                if (contentStart < 8) {
                    throw new RuntimeException("无法解析LLM响应格式");
                }
            }

            int contentEnd = responseBody.indexOf("\"", contentStart);
            if (contentEnd < 0) {
                throw new RuntimeException("无法找到LLM响应内容结束标记");
            }

            // 提取并清理SQL内容
            String sql = responseBody.substring(contentStart, contentEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            // 移除可能的代码块标记
            sql = sql.replace("```sql", "").replace("```", "").trim();

            return sql;
        } catch (Exception e) {
            System.err.println("解析LLM响应失败: " + responseBody);
            throw new RuntimeException("无法从LLM响应中解析SQL", e);
        }
    }
}