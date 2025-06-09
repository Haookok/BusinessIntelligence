package com.businessintelligence.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class DataWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DataWebSocketHandler() {
        scheduler.scheduleAtFixedRate(this::sendRealtimeData, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private void sendRealtimeData() {
        // 模拟从数据库获取数据（请替换为实际查询）
        String jsonData = getLatestDataFromDB();

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getLatestDataFromDB() {
        // TODO: 替换为实际 MySQL 查询
        int random = new Random().nextInt(100);
        return "{\"value\": " + random + ", \"timestamp\": " + System.currentTimeMillis() + "}";
    }
}
