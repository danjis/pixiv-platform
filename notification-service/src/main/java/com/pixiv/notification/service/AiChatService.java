package com.pixiv.notification.service;

import com.pixiv.notification.dto.AiChatMessageDTO;
import com.pixiv.notification.dto.AiChatSessionDTO;
import com.pixiv.notification.entity.AiChatMessage;
import com.pixiv.notification.entity.AiChatSession;
import com.pixiv.notification.repository.AiChatMessageRepository;
import com.pixiv.notification.repository.AiChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatService.class);

    @Autowired
    private AiChatSessionRepository sessionRepository;

    @Autowired
    private AiChatMessageRepository messageRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * 创建新会话
     */
    @Transactional
    public AiChatSessionDTO createSession(Long userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle("AI 助手对话");
        session.setStatus("ACTIVE");
        session = sessionRepository.save(session);

        logger.info("创建 AI 聊天会话: userId={}, sessionId={}", userId, session.getId());
        return toSessionDTO(session, null);
    }

    /**
     * 获取用户会话列表
     */
    public List<AiChatSessionDTO> getUserSessions(Long userId, int page, int size) {
        Page<AiChatSession> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(
                userId, PageRequest.of(page, size));

        return sessions.getContent().stream().map(session -> {
            // 获取最后一条消息
            List<AiChatMessage> lastMsgs = messageRepository.findBySessionIdOrderByCreatedAtDesc(
                    session.getId(), PageRequest.of(0, 1));
            String lastMsg = lastMsgs.isEmpty() ? null : lastMsgs.get(0).getContent();
            return toSessionDTO(session, lastMsg);
        }).collect(Collectors.toList());
    }

    /**
     * 获取会话的聊天记录
     */
    public List<AiChatMessageDTO> getSessionMessages(Long userId, Long sessionId) {
        // 验证会话归属
        AiChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    /**
     * 发送消息并获取AI回复
     */
    @Transactional
    public AiChatMessageDTO sendMessage(Long userId, Long sessionId, String message) {
        // 验证会话归属
        AiChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        // 保存用户消息
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageRepository.save(userMsg);

        // 获取最近对话历史
        List<AiChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 调用 AI 服务
        String aiReply;
        List<String> suggestions;
        try {
            Map<String, Object> result = callAiService(message, history);
            aiReply = (String) result.get("reply");
            suggestions = (List<String>) result.getOrDefault("suggestions", Collections.emptyList());
        } catch (Exception e) {
            logger.error("调用 AI 服务失败: {}", e.getMessage());
            aiReply = "抱歉，我暂时无法回复，请稍后再试或联系人工客服 😊";
            suggestions = Collections.emptyList();
        }

        // 保存AI回复
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiReply);
        messageRepository.save(aiMsg);

        // 更新会话标题（如果是首条消息）
        if (history.size() <= 1) {
            String title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
            session.setTitle(title);
            sessionRepository.save(session);
        }

        AiChatMessageDTO dto = toMessageDTO(aiMsg);
        return dto;
    }

    /**
     * 发送消息（返回回复+推荐问题）
     */
    @Transactional
    public Map<String, Object> sendMessageWithSuggestions(Long userId, Long sessionId, String message) {
        // 验证会话归属
        AiChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        // 保存用户消息
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageRepository.save(userMsg);

        // 获取最近对话历史
        List<AiChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 调用 AI 服务
        String aiReply;
        List<String> suggestions;
        try {
            Map<String, Object> result = callAiService(message, history);
            aiReply = (String) result.get("reply");
            suggestions = (List<String>) result.getOrDefault("suggestions", Collections.emptyList());
        } catch (Exception e) {
            logger.error("调用 AI 服务失败: {}", e.getMessage());
            aiReply = "抱歉，我暂时无法回复，请稍后再试或联系人工客服 😊";
            suggestions = List.of("平台有哪些功能？", "如何发起约稿？", "如何成为画师？");
        }

        // 保存AI回复
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiReply);
        messageRepository.save(aiMsg);

        // 更新会话标题（第一条消息时）
        if (history.size() <= 1) {
            String title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
            session.setTitle(title);
            sessionRepository.save(session);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", toMessageDTO(aiMsg));
        response.put("suggestions", suggestions);
        return response;
    }

    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        AiChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        sessionRepository.delete(session);
        logger.info("删除 AI 聊天会话: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 调用 Python AI 服务
     */
    private Map<String, Object> callAiService(String message, List<AiChatMessage> history) {
        String url = aiServiceUrl + "/api/chat";

        // 构建历史消息（只传最近20条，跳过system消息）
        List<Map<String, String>> historyList = history.stream()
                .filter(m -> !"system".equals(m.getRole()))
                .map(m -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", m.getRole());
                    map.put("content", m.getContent());
                    return map;
                })
                .collect(Collectors.toList());

        // 限制历史条数
        if (historyList.size() > 20) {
            historyList = historyList.subList(historyList.size() - 20, historyList.size());
        }
        // 移除最后一条（就是刚保存的用户消息，会在 ai-service 的 message 字段传入）
        if (!historyList.isEmpty()) {
            historyList = historyList.subList(0, historyList.size() - 1);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("history", historyList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        logger.info("调用 AI 服务: url={}, message={}", url, message);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("AI 服务返回异常: " + response.getStatusCode());
    }

    private AiChatSessionDTO toSessionDTO(AiChatSession session, String lastMessage) {
        AiChatSessionDTO dto = new AiChatSessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        dto.setLastMessage(lastMessage);
        return dto;
    }

    private AiChatMessageDTO toMessageDTO(AiChatMessage message) {
        AiChatMessageDTO dto = new AiChatMessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
