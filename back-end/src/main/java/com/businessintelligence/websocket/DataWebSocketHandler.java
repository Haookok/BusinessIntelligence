package com.businessintelligence.websocket;

import com.businessintelligence.DTO.MinutelyBrowseDTO;
import com.businessintelligence.repository.NewsLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("DataWebSocketScheduler");
        t.setDaemon(true);
        return t;
    });

    private final NewsLiveRepository newsLiveRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean started = new AtomicBoolean(false);

    // 每个session独立的lastSentTimestamp & newsId
    private final ConcurrentHashMap<WebSocketSession, Integer> sessionNewsIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Long> sessionLastTimestampMap = new ConcurrentHashMap<>();

    public DataWebSocketHandler(NewsLiveRepository newsLiveRepository) {
        this.newsLiveRepository = newsLiveRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected, session id: " + session.getId());

        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendRealtimeData();
                } catch (Exception e) {
                    System.err.println("Unhandled exception in scheduled task:");
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);
            System.out.println("Scheduled task started.");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            int newsId = Integer.parseInt(payload.trim());
            sessionNewsIdMap.put(session, newsId);
            sessionLastTimestampMap.put(session, null); // 初始化时间戳
            System.out.println("Received newsId " + newsId + " from session " + session.getId());
        } catch (NumberFormatException e) {
            System.err.println("Invalid newsId format from session " + session.getId() + ": " + message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionNewsIdMap.remove(session);
        sessionLastTimestampMap.remove(session);
        System.out.println("WebSocket closed, session id: " + session.getId());

        if (sessions.isEmpty()) {
            System.out.println("All sessions closed. Data send logic will auto-pause.");
        }
    }

    private void sendRealtimeData() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions, skipping sendRealtimeData");
            return;
        }

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) continue;

            Integer newsId = sessionNewsIdMap.get(session);
            if (newsId == null) continue;

            Long lastTimestamp = sessionLastTimestampMap.get(session);
            List<Object[]> rawList;

            if (lastTimestamp == null) {
                System.out.println("Full query for session " + session.getId());
                rawList = newsLiveRepository.getSecondlyBrowseInitial(newsId);
            } else {
                System.out.println("Incremental query for session " + session.getId() + " since " + lastTimestamp);
                rawList = newsLiveRepository.getSecondlyBrowseSince(newsId, lastTimestamp);
            }

            List<MinutelyBrowseDTO> dtoList = new ArrayList<>();
            long maxTimestamp = lastTimestamp != null ? lastTimestamp : 0;

            for (Object[] row : rawList) {
                long ts = ((Number) row[0]).longValue();
                long count = ((Number) row[1]).longValue();
                dtoList.add(new MinutelyBrowseDTO(ts, count));
                if (ts > maxTimestamp) maxTimestamp = ts;
            }

            if (!dtoList.isEmpty()) {
                sessionLastTimestampMap.put(session, maxTimestamp + 1);
            }

            try {
                String json = objectMapper.writeValueAsString(dtoList);
                session.sendMessage(new TextMessage(json));
                System.out.println("Sent data to session: " + session.getId());
            } catch (IOException e) {
                System.err.println("Failed to send data to session " + session.getId());
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void onDestroy() {
        scheduler.shutdownNow();
        System.out.println("Scheduler shutdown on bean destroy.");
    }
}
