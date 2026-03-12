package com.pixiv.file.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.pixiv.file.config.OssProperties;
import com.pixiv.file.constant.FileConstants;
import com.pixiv.file.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 * 
 * 负责文件上传、图片处理、OSS 存储等功能
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    /**
     * 上传头像文件
     *
     * 头像上传到 OSS 的 avatars/ 目录，不生成缩略图
     *
     * @param file 上传的头像文件
     * @return 上传响应（仅包含头像 URL，无缩略图）
     * @throws IOException 文件处理异常
     */
    public UploadResponse uploadAvatar(MultipartFile file) throws IOException {
        log.info("开始上传头像，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        // 1. 验证文件（使用头像专用校验）
        validateAvatarFile(file);

        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFileName = generateUniqueFileName(extension);

        // 3. 上传头像到 avatars/ 目录
        String avatarPath = FileConstants.AVATAR_PATH_PREFIX + uniqueFileName;
        String avatarUrl = uploadToOss(file.getInputStream(), avatarPath, file.getContentType(), file.getSize());
        log.info("头像上传成功: {}", avatarUrl);

        // 4. 构建响应（头像不需要缩略图）
        return UploadResponse.builder()
                .imageUrl(avatarUrl)
                .thumbnailUrl(null)
                .fileName(uniqueFileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 上传图片文件
     * 
     * @param file 上传的文件
     * @return 上传响应（包含原图和缩略图 URL）
     * @throws IOException 文件处理异常
     */
    public UploadResponse uploadImage(MultipartFile file) throws IOException {
        log.info("开始上传图片，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        // 1. 验证文件
        validateFile(file);

        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFileName = generateUniqueFileName(extension);

        // 3. 上传原始图片（无水印，VIP专享）
        String originalPath = FileConstants.ORIGINAL_PATH_PREFIX + uniqueFileName;
        String originalImageUrl = uploadToOss(file.getInputStream(), originalPath, file.getContentType(),
                file.getSize());
        log.info("原始图片（无水印）上传成功: {}", originalImageUrl);

        // 4. 为原图添加水印
        byte[] watermarkedBytes = addWatermark(file.getInputStream(), extension);
        ByteArrayInputStream watermarkedStream = new ByteArrayInputStream(watermarkedBytes);

        // 5. 上传带水印的图片
        String imagePath = FileConstants.OSS_PATH_PREFIX + uniqueFileName;
        String imageUrl = uploadToOss(watermarkedStream, imagePath, file.getContentType(), watermarkedBytes.length);
        log.info("水印图片上传成功: {}", imageUrl);

        // 6. 生成并上传缩略图（基于带水印的图片）
        String thumbnailPath = FileConstants.THUMBNAIL_PATH_PREFIX + uniqueFileName;
        String thumbnailUrl = createAndUploadThumbnail(file, thumbnailPath);
        log.info("缩略图上传成功: {}", thumbnailUrl);

        // 7. 构建响应
        return UploadResponse.builder()
                .imageUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .originalImageUrl(originalImageUrl)
                .fileName(uniqueFileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 验证文件
     * 
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > FileConstants.MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("文件大小超过限制，最大允许 %d MB", FileConstants.MAX_FILE_SIZE / 1024 / 1024));
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !FileConstants.ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "不支持的文件类型，仅支持: " + String.join(", ", FileConstants.ALLOWED_IMAGE_TYPES));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!FileConstants.ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "不支持的文件扩展名，仅支持: " + String.join(", ", FileConstants.ALLOWED_EXTENSIONS));
        }

        log.debug("文件验证通过: {}", originalFilename);
    }

    /**
     * 验证头像文件（大小限制 5MB）
     *
     * @param file 上传的头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("头像文件不能为空");
        }

        if (file.getSize() > FileConstants.MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException(
                    String.format("头像文件大小超过限制，最大允许 %d MB", FileConstants.MAX_AVATAR_SIZE / 1024 / 1024));
        }

        String contentType = file.getContentType();
        if (contentType == null || !FileConstants.ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "不支持的文件类型，仅支持: " + String.join(", ", FileConstants.ALLOWED_IMAGE_TYPES));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!FileConstants.ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "不支持的文件扩展名，仅支持: " + String.join(", ", FileConstants.ALLOWED_EXTENSIONS));
        }

        log.debug("头像文件验证通过: {}", originalFilename);
    }

    /**
     * 生成唯一文件名
     * 
     * 格式: yyyyMMdd/UUID.extension
     * 例如: 20240101/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
     * 
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String extension) {
        // 使用日期作为目录，便于管理
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 使用 UUID 生成唯一标识
        String uuid = UUID.randomUUID().toString();

        return String.format("%s/%s.%s", datePrefix, uuid, extension.toLowerCase());
    }

    /**
     * 上传文件到 OSS
     * 
     * @param inputStream   文件输入流
     * @param objectKey     OSS 对象键（文件路径）
     * @param contentType   文件类型
     * @param contentLength 文件大小
     * @return 文件访问 URL
     */
    private String uploadToOss(InputStream inputStream, String objectKey, String contentType, long contentLength) {
        try {
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);

            // 上传文件
            PutObjectResult result = ossClient.putObject(
                    ossProperties.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata);

            log.debug("文件上传到 OSS 成功，ETag: {}", result.getETag());

            // 返回文件访问 URL
            return generateFileUrl(objectKey);

        } catch (Exception e) {
            log.error("上传文件到 OSS 失败: {}", objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件访问 URL
     * 
     * @param objectKey OSS 对象键
     * @return 文件访问 URL
     */
    private String generateFileUrl(String objectKey) {
        // 格式: https://bucket-name.endpoint/object-key
        return String.format("https://%s.%s/%s",
                ossProperties.getBucketName(),
                ossProperties.getEndpoint(),
                objectKey);
    }

    /**
     * 创建并上传缩略图
     * 
     * @param file          原始文件
     * @param thumbnailPath 缩略图路径
     * @return 缩略图 URL
     * @throws IOException 文件处理异常
     */
    private String createAndUploadThumbnail(MultipartFile file, String thumbnailPath) throws IOException {
        // 生成缩略图
        byte[] thumbnailBytes = createThumbnail(file);

        // 上传缩略图
        ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailBytes);
        return uploadToOss(
                thumbnailInputStream,
                thumbnailPath,
                file.getContentType(),
                thumbnailBytes.length);
    }

    /**
     * 为图片添加文字水印
     * 
     * 在图片右下角添加半透明的 "© PixivPlatform" 水印文字
     * 
     * @param inputStream 原图输入流
     * @param extension   文件扩展名
     * @return 带水印的图片字节数组
     * @throws IOException 图片处理异常
     */
    private byte[] addWatermark(InputStream inputStream, String extension) throws IOException {
        log.debug("开始添加水印");
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("无法解析图片文件");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 创建 Graphics2D 绘制水印
        Graphics2D g2d = originalImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 水印文字
        String watermarkText = "© PixivPlatform";

        // 根据图片大小动态调整字体大小（图片越大水印字体越大）
        int fontSize = Math.max(16, Math.min(width, height) / 30);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        // 计算水印文字尺寸
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(watermarkText);
        int textHeight = fm.getHeight();

        // 水印位置：右下角，留出 margin
        int margin = fontSize;
        int x = width - textWidth - margin;
        int y = height - margin;

        // 绘制阴影（提高可读性）
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawString(watermarkText, x + 2, y + 2);

        // 绘制水印文字（半透明白色）
        g2d.setColor(new Color(255, 255, 255, 128));
        g2d.drawString(watermarkText, x, y);

        g2d.dispose();

        // 输出为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = extension.equalsIgnoreCase("png") ? "png" : "jpg";
        ImageIO.write(originalImage, formatName, baos);
        byte[] result = baos.toByteArray();
        log.debug("水印添加成功，输出大小: {} bytes", result.length);
        return result;
    }

    /**
     * 删除 OSS 上的单个文件
     *
     * @param fileUrl 文件的完整 URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            String objectKey = extractObjectKey(fileUrl);
            if (objectKey == null) {
                log.warn("无法从 URL 提取 OSS 对象键: {}", fileUrl);
                return false;
            }
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
            log.info("OSS 文件删除成功: {}", objectKey);
            return true;
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 批量删除 OSS 上的文件
     *
     * @param fileUrls 文件的完整 URL 列表
     * @return 成功删除的数量
     */
    public int deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        List<String> objectKeys = new ArrayList<>();
        for (String url : fileUrls) {
            String key = extractObjectKey(url);
            if (key != null) {
                objectKeys.add(key);
            }
        }
        if (objectKeys.isEmpty()) {
            return 0;
        }
        try {
            // 阿里云 OSS 批量删除（每次最多 1000 个）
            com.aliyun.oss.model.DeleteObjectsRequest deleteRequest = new com.aliyun.oss.model.DeleteObjectsRequest(
                    ossProperties.getBucketName())
                    .withKeys(objectKeys)
                    .withQuiet(true);
            ossClient.deleteObjects(deleteRequest);
            successCount = objectKeys.size();
            log.info("OSS 批量删除成功，共 {} 个文件", successCount);
        } catch (Exception e) {
            log.error("OSS 批量删除异常，尝试逐个删除", e);
            for (String key : objectKeys) {
                try {
                    ossClient.deleteObject(ossProperties.getBucketName(), key);
                    successCount++;
                } catch (Exception ex) {
                    log.error("OSS 文件删除失败: {}", key, ex);
                }
            }
        }
        return successCount;
    }

    /**
     * 从文件 URL 中提取 OSS 对象键
     * URL 格式: https://{bucket}.{endpoint}/{objectKey}
     *
     * @param fileUrl 文件完整 URL
     * @return OSS 对象键，提取失败返回 null
     */
    private String extractObjectKey(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String prefix = String.format("https://%s.%s/",
                ossProperties.getBucketName(), ossProperties.getEndpoint());
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        // 兼容 http
        String httpPrefix = String.format("http://%s.%s/",
                ossProperties.getBucketName(), ossProperties.getEndpoint());
        if (fileUrl.startsWith(httpPrefix)) {
            return fileUrl.substring(httpPrefix.length());
        }
        log.warn("URL 不匹配当前 OSS 配置: {}", fileUrl);
        return null;
    }

    /**
     * 创建缩略图
     * 
     * 使用 Thumbnailator 库生成缩略图
     * 
     * @param file 原始文件
     * @return 缩略图字节数组
     * @throws IOException 文件处理异常
     */
    public byte[] createThumbnail(MultipartFile file) throws IOException {
        log.debug("开始生成缩略图，目标尺寸: {}x{}, 质量: {}",
                ossProperties.getThumbnail().getWidth(),
                ossProperties.getThumbnail().getHeight(),
                ossProperties.getThumbnail().getQuality());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 使用 Thumbnailator 生成缩略图
        Thumbnails.of(file.getInputStream())
                .size(
                        ossProperties.getThumbnail().getWidth(),
                        ossProperties.getThumbnail().getHeight())
                .outputQuality(ossProperties.getThumbnail().getQuality())
                .toOutputStream(outputStream);

        byte[] thumbnailBytes = outputStream.toByteArray();
        log.debug("缩略图生成成功，大小: {} bytes", thumbnailBytes.length);

        return thumbnailBytes;
    }
}
