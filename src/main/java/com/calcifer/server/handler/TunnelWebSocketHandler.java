package com.calcifer.server.handler;

import com.calcifer.server.service.TunnelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class TunnelWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private TunnelService tunnelService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        tunnelService.registerSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        tunnelService.handleIncomingMessage(message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        tunnelService.removeSession(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket连接错误", exception);
        if(session.isOpen()){
            session.close();
        }
        tunnelService.removeSession(session);
    }
}
