package com.calcifer.server.config;

import com.calcifer.server.handler.TunnelWebSocketHandler;
import com.calcifer.server.handler.WebWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TunnelWebSocketHandler tunnelWebSocketHandler;
    
    @Autowired
    private WebWebSocketHandler webWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 内网隧道连接点
        registry.addHandler(tunnelWebSocketHandler, "/ws-tunnel")
                .setAllowedOrigins("*");
        
        // 前端浏览器连接点
        registry.addHandler(webWebSocketHandler, "/api-ws")
                .setAllowedOrigins("*");
    }
}
