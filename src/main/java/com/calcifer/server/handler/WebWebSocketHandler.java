package com.calcifer.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class WebWebSocketHandler extends TextWebSocketHandler {

    // 线程安全的 Session 集合
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("前端用户已连接: {}, 当前在线人数: {}", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("前端用户已断开: {}, 当前在线人数: {}", session.getId(), sessions.size());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("前端WebSocket连接错误", exception);
        if(session.isOpen()){
            session.close();
        }
        sessions.remove(session);
    }

    /**
     * 广播消息给所有前端用户
     */
    public void broadcast(String message) {
        if (sessions.isEmpty()) return;
        
        TextMessage textMessage = new TextMessage(message);
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("发送消息给前端失败: {}", session.getId(), e);
            }
        });
    }
}
