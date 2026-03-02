package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码控制器
 * 处理图形验证码和邮箱验证码相关请求
 */
@RestController
@RequestMapping("/api/captcha")
@Tag(name = "验证码接口", description = "图形验证码和邮箱验证码相关接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 获取图形验证码
     *
     * @return 验证码ID和图片（Base64）
     */
    @GetMapping("/image")
    @Operation(
            summary = "获取图形验证码",
            description = "生成图形验证码，返回验证码ID和Base64编码的图片。\n\n" +
                    "**使用说明：**\n" +
                    "1. 前端调用此接口获取验证码ID和图片\n" +
                    "2. 显示图片给用户\n" +
                    "3. 用户输入验证码后，连同验证码ID一起提交"
    )
    @SecurityRequirement(name = "")  // 不需要认证
    public ResponseEntity<Result<Map<String, String>>> getImageCaptcha() {
        // 生成唯一的验证码ID
        String captchaId = UUID.randomUUID().toString();

        // 生成验证码图片
        String imageBase64 = captchaService.generateImageCaptcha(captchaId);

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("image", imageBase64);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 发送邮箱验证码
     *
     * @param email       邮箱地址
     * @param captchaId   图形验证码ID
     * @param captchaCode 图形验证码
     * @return 成功响应
     */
    @PostMapping("/email")
    @Operation(
            summary = "发送邮箱验证码",
            description = "验证图形验证码后，向指定邮箱发送验证码。\n\n" +
                    "**使用说明：**\n" +
                    "1. 用户先获取图形验证码\n" +
                    "2. 用户输入图形验证码\n" +
                    "3. 调用此接口发送邮箱验证码\n" +
                    "4. 用户在邮箱中查收验证码\n\n" +
                    "**注意事项：**\n" +
                    "- 图形验证码验证失败会返回错误\n" +
                    "- 同一邮箱10分钟内只能发送一次验证码"
    )
    @SecurityRequirement(name = "")  // 不需要认证
    public ResponseEntity<Result<Void>> sendEmailCode(
            @Parameter(description = "邮箱地址", required = true, example = "user@example.com")
            @RequestParam("email") String email,

            @Parameter(description = "图形验证码ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam("captchaId") String captchaId,

            @Parameter(description = "图形验证码", required = true, example = "AB3D")
            @RequestParam("captchaCode") String captchaCode) {

        // 1. 验证图形验证码
        if (!captchaService.verifyImageCaptcha(captchaId, captchaCode)) {
            return ResponseEntity.ok(Result.error("图形验证码错误或已过期"));
        }

        // 2. 发送邮箱验证码
        captchaService.sendEmailCode(email);

        return ResponseEntity.ok(Result.success("验证码已发送，请查收邮件", null));
    }
}
