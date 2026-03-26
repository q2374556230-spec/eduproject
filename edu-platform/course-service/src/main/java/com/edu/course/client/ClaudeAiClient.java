package com.edu.course.client;

import com.edu.course.dto.CourseVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claude AI 客户端
 * 调用 Anthropic Claude API 实现AI课程推荐功能
 */
@Slf4j
@Component
public class ClaudeAiClient {

    @Value("${ai.claude.api-key:your-api-key-here}")
    private String apiKey;

    @Value("${ai.claude.model:claude-sonnet-4-6}")
    private String model;

    @Value("${ai.claude.max-tokens:1024}")
    private Integer maxTokens;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ClaudeAiClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.anthropic.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * 发送消息给 Claude，返回文本响应
     */
    public String chat(String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        Map<?, ?> response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Claude API返回空响应");
        }

        // 解析响应: response.content[0].text
        List<?> content = (List<?>) response.get("content");
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Claude API响应内容为空");
        }

        Map<?, ?> firstContent = (Map<?, ?>) content.get(0);
        String text = (String) firstContent.get("text");
        log.debug("Claude API响应: {}", text);
        return text;
    }

    /**
     * 解析AI推荐结果，匹配课程列表
     */
    public List<CourseVO> parseRecommendations(String aiResponse, List<CourseVO> allCourses) {
        try {
            // 提取JSON部分（AI可能会多说几个字）
            String json = aiResponse;
            int start = aiResponse.indexOf('[');
            int end = aiResponse.lastIndexOf(']');
            if (start >= 0 && end > start) {
                json = aiResponse.substring(start, end + 1);
            }

            List<Map<String, Object>> recommendations = objectMapper.readValue(
                    json, new TypeReference<>() {});

            // 根据推荐ID匹配课程并填充推荐理由
            Map<Long, CourseVO> courseMap = allCourses.stream()
                    .collect(Collectors.toMap(CourseVO::getId, c -> c));

            return recommendations.stream()
                    .map(rec -> {
                        Object idObj = rec.get("id");
                        Long id = idObj instanceof Number ? ((Number) idObj).longValue() : null;
                        String reason = (String) rec.get("reason");
                        CourseVO course = courseMap.get(id);
                        if (course != null) {
                            course.setRecommendReason(reason);
                        }
                        return course;
                    })
                    .filter(c -> c != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析AI推荐结果失败: {}", e.getMessage());
            return List.of();
        }
    }
}
