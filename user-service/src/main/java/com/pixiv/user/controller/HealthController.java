package com.pixiv.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * 用于验证服务是否正常运行
 * 
 * @author Pixiv Platform Team
 */
@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "服务健康检查接口")
public class HealthController {

    @GetMapping
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "user-service");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "用户服务运行正常");
        return result;
    }

}
