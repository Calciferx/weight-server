package com.calcifer.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 隧道通信消息协议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelMessage implements Serializable {
    /**
     * 消息唯一ID，用于请求响应匹配
     */
    private String id;

    /**
     * 消息类型
     * CMD: 公网发给内网的指令 (如开闸)
     * RESP: 内网发给公网的指令响应
     * EVENT: 内网发给公网的实时事件 (如实时重量)
     */
    private String type;

    /**
     * 具体的动作标识 (如 "barrier", "init")
     */
    private String action;

    /**
     * 数据载荷 (JSON字符串或对象)
     */
    private Object data;

    /**
     * 响应是否成功 (仅 RESP 类型有效)
     */
    private boolean success;

    /**
     * 响应消息 (仅 RESP 类型有效)
     */
    private String message;
}
