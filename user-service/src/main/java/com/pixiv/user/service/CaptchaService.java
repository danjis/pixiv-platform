package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 处理图形验证码和邮箱验证码的生成、验证
 */
@Service
public class CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;  // 从配置文件读取发件人邮箱

    // Redis 键前缀
    private static final String IMAGE_CAPTCHA_PREFIX = "captcha:image:";
    private static final String EMAIL_CODE_PREFIX = "captcha:email:";

    // 验证码配置
    private static final int IMAGE_CAPTCHA_LENGTH = 4;  // 图形验证码长度
    private static final int EMAIL_CODE_LENGTH = 6;     // 邮箱验证码长度
    private static final int IMAGE_CAPTCHA_EXPIRE = 5;  // 图形验证码过期时间（分钟）
    private static final int EMAIL_CODE_EXPIRE = 10;    // 邮箱验证码过期时间（分钟）

    // 图形验证码字符集（去除容易混淆的字符）
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    public CaptchaService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    /**
     * 生成图形验证码
     *
     * @param captchaId 验证码ID
     * @return Base64 编码的图片
     */
    public String generateImageCaptcha(String captchaId) {
        logger.info("生成图形验证码: captchaId={}", captchaId);

        // 1. 生成随机验证码文本
        String captchaText = generateRandomString(IMAGE_CAPTCHA_LENGTH, CAPTCHA_CHARS);

        // 2. 存储到 Redis（不区分大小写）
        String key = IMAGE_CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, captchaText.toUpperCase(), IMAGE_CAPTCHA_EXPIRE, TimeUnit.MINUTES);

        // 3. 生成图片
        BufferedImage image = createCaptchaImage(captchaText);

        // 4. 转换为 Base64
        String base64Image = imageToBase64(image);

        logger.info("图形验证码生成成功: captchaId={}", captchaId);
        return base64Image;
    }

    /**
     * 验证图形验证码
     *
     * @param captchaId   验证码ID
     * @param captchaCode 用户输入的验证码
     * @return 是否验证成功
     */
    public boolean verifyImageCaptcha(String captchaId, String captchaCode) {
        logger.info("验证图形验证码: captchaId={}, captchaCode={}", captchaId, captchaCode);

        String key = IMAGE_CAPTCHA_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            logger.warn("图形验证码不存在或已过期: captchaId={}", captchaId);
            return false;
        }

        // 验证成功后删除验证码（一次性使用）
        redisTemplate.delete(key);

        boolean isValid = storedCode.equalsIgnoreCase(captchaCode);
        logger.info("图形验证码验证结果: captchaId={}, isValid={}", captchaId, isValid);
        return isValid;
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     */
    public void sendEmailCode(String email) {
        logger.info("发送邮箱验证码: email={}", email);

        // 1. 生成6位数字验证码
        String code = generateRandomString(EMAIL_CODE_LENGTH, "0123456789");

        // 2. 存储到 Redis
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, EMAIL_CODE_EXPIRE, TimeUnit.MINUTES);

        // 3. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);  // 使用配置文件中的发件人邮箱
            message.setTo(email);
            message.setSubject("Pixiv Platform - 邮箱验证码");
            message.setText(String.format(
                    "您的验证码是：%s\n\n" +
                    "验证码有效期为 %d 分钟，请勿泄露给他人。\n\n" +
                    "如果这不是您的操作，请忽略此邮件。",
                    code, EMAIL_CODE_EXPIRE
            ));

            mailSender.send(message);
            logger.info("邮箱验证码发送成功: email={}", email);
        } catch (Exception e) {
            logger.error("邮箱验证码发送失败: email={}, error={}", email, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     * @return 是否验证成功
     */
    public boolean verifyEmailCode(String email, String code) {
        logger.info("验证邮箱验证码: email={}, code={}", email, code);

        String key = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            logger.warn("邮箱验证码不存在或已过期: email={}", email);
            return false;
        }

        // 验证成功后删除验证码（一次性使用）
        redisTemplate.delete(key);

        boolean isValid = storedCode.equals(code);
        logger.info("邮箱验证码验证结果: email={}, isValid={}", email, isValid);
        return isValid;
    }

    /**
     * 生成随机字符串
     *
     * @param length  长度
     * @param charset 字符集
     * @return 随机字符串
     */
    private String generateRandomString(int length, String charset) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }
        return sb.toString();
    }

    /**
     * 创建验证码图片
     *
     * @param captchaText 验证码文本
     * @return 图片对象
     */
    private BufferedImage createCaptchaImage(String captchaText) {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 绘制干扰线
        Random random = new Random();
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文本
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < captchaText.length(); i++) {
            // 随机颜色
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            
            // 随机位置和角度
            int x = 20 + i * 25;
            int y = 25 + random.nextInt(10);
            double angle = (random.nextDouble() - 0.5) * 0.4;
            
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(captchaText.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        g.dispose();
        return image;
    }

    /**
     * 将图片转换为 Base64 字符串
     *
     * @param image 图片对象
     * @return Base64 字符串
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            logger.error("图片转换失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码生成失败");
        }
    }
}
