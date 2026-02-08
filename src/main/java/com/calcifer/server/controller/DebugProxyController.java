package com.calcifer.server.controller;

import com.calcifer.server.model.TunnelMessage;
import com.calcifer.server.service.TunnelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugProxyController {

    @Autowired
    private TunnelService tunnelService;

    @PostMapping("/sql")
    public Object executeSql(@RequestBody Map<String, String> body) {
        String sql = body.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return Map.of("success", false, "message", "SQL cannot be empty");
        }

        log.warn("Receiving SQL execution request: {}", sql);

        try {
            // 发送指令到内网
            TunnelMessage response = tunnelService.sendCommand("exec_sql", Map.of("sql", sql));
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                return Map.of("success", false, "message", response.getMessage());
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Execution failed: " + e.getMessage());
        }
    }
}
