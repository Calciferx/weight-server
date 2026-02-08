package com.calcifer.server.service;

import com.alibaba.fastjson2.JSON;
import com.calcifer.server.model.TunnelMessage;
import com.calcifer.server.handler.WebWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TunnelService {

    @Autowired
    private WebWebSocketHandler webWebSocketHandler;

    // 存储当前活跃的内网连接 Session (假设只有一个内网客户端)
    private WebSocketSession activeSession;
    
    // 存储前端广播 Session (简单起见，这里引用 Handler 里的集合，或者通过事件发布)
    // 这里暂时简化，重点关注 RPC 部分

    // 存储等待响应的请求: RequestId -> Future
    private final Map<String, CompletableFuture<TunnelMessage>> pendingRequests = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        this.activeSession = session;
        log.info("内网客户端已连接: {}", session.getId());
    }

    public void removeSession(WebSocketSession session) {
        if (this.activeSession == session) {
            this.activeSession = null;
            log.info("内网客户端断开连接");
        }
    }

    /**
     * 发送指令到内网并等待响应
     */
    public TunnelMessage sendCommand(String action, Object data) {
        if (activeSession == null || !activeSession.isOpen()) {
            throw new RuntimeException("内网服务未连接");
        }

        String requestId = java.util.UUID.randomUUID().toString();
        TunnelMessage cmd = TunnelMessage.builder()
                .id(requestId)
                .type("CMD")
                .action(action)
                .data(data)
                .build();

        CompletableFuture<TunnelMessage> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            String json = JSON.toJSONString(cmd);
            activeSession.sendMessage(new TextMessage(json));
            log.info("发送指令到内网: id={}, action={}", requestId, action);

            // 等待响应，设置超时时间 10秒
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            throw new RuntimeException("指令执行超时或失败: " + e.getMessage());
        }
    }

    /**
     * 处理来自内网的消息
     */
    public void handleIncomingMessage(String payload) {
        try {
            TunnelMessage msg = JSON.parseObject(payload, TunnelMessage.class);
            if (msg == null || msg.getType() == null) return;

            switch (msg.getType()) {
                case "RESP":
                    handleResponse(msg);
                    break;
                case "EVENT":
                    handleEvent(msg);
                    break;
                default:
                    log.warn("未知消息类型: {}", msg.getType());
            }
        } catch (Exception e) {
            log.error("处理消息异常", e);
        }
    }

    private void handleResponse(TunnelMessage msg) {
        CompletableFuture<TunnelMessage> future = pendingRequests.remove(msg.getId());
        if (future != null) {
            future.complete(msg);
        } else {
            log.warn("收到响应但找不到对应的请求: {}", msg.getId());
        }
    }

    private void handleEvent(TunnelMessage msg) {
        // 广播给所有前端 WebSocket
        if (msg.getData() != null) {
            // 直接转发 data 部分，或者根据协议重新封装
            // 这里假设前端直接接收 data 里的内容，因为内网发来的 data 已经是前端需要的格式了
            // 但要注意 TunnelMessage 包装了一层，前端可能不需要最外层的 id/type/action
            // 根据 weightweb 的代码，它期望收到 {type: "RT_WEIGH_NUM", data: ...}
            // 我们假设 TunnelMessage.data 就是这个完整的对象
            String json = JSON.toJSONString(msg.getData());
            webWebSocketHandler.broadcast(json);
        }
    }
}
