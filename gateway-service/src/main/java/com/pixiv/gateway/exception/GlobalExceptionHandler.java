package com.pixiv.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixiv.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 全局异常处理器
 * 
 * 功能：
 * - 捕获网关层的所有异常
 * - 返回统一格式的错误响应
 * - 记录错误日志
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 根据异常类型设置 HTTP 状态码和错误信息
        HttpStatus status;
        String message;

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : "请求处理失败";
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = "请求参数错误";
        } else if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "服务不可用";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "服务器内部错误";
        }

        // 记录错误日志
        log.error("网关异常 - 状态码: {}, 错误信息: {}, 异常: {}", 
                status.value(), message, ex.getMessage(), ex);

        // 设置响应状态码
        response.setStatusCode(status);

        // 构建错误响应
        Result<Void> result = Result.error(status.value(), message);

        try {
            byte[] bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }

}
