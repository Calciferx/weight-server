package com.calcifer.server.controller;

import com.calcifer.server.model.TunnelMessage;
import com.calcifer.server.service.TunnelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceProxyController {

    @Autowired
    private TunnelService tunnelService;

    @PostMapping("/barrier")
    public Object barrier(@RequestParam Map<String, Object> params) {
        // 前端传参: device (FRONT_BARRIER_ON 等)
        // 后端需要 ModBusDeviceEnum
        return executeProxy("barrier", params);
    }

    @PostMapping("/light")
    public Object light(@RequestParam Map<String, Object> params) {
        // 前端传参: device, status (boolean)
        return executeProxy("light", params);
    }

    @PostMapping("/init")
    public Object init() {
        return executeProxy("init", null);
    }
    
    /**
     * 执行代理请求
     */
    private Object executeProxy(String action, Object data) {
        try {
            TunnelMessage response = tunnelService.sendCommand(action, data);
            if (response.isSuccess()) {
                // 如果后端返回的是标准 RespWrapper 结构，它可能包含在 response.getData() 里
                // 这里我们直接返回 data 部分，或者根据需要封装
                // 假设内网返回的 TunnelMessage.data 就是 RespWrapper 的 JSON 或对象
                return response.getData();
            } else {
                // 如果 TunnelMessage 本身标记为失败（比如超时，或内网报错）
                // 构造一个错误的响应返回给前端
                return Map.of("success", false, "message", response.getMessage() != null ? response.getMessage() : "请求失败");
            }
        } catch (Exception e) {
            log.error("代理请求失败", e);
            return Map.of("success", false, "message", "系统内部错误: " + e.getMessage());
        }
    }
}
