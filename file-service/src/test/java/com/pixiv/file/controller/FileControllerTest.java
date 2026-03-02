package com.pixiv.file.controller;

import com.pixiv.file.service.FileStorageService;
import com.pixiv.file.dto.UploadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件控制器测试
 * 
 * @author Pixiv Platform Team
 */
@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    /**
     * 测试上传图片接口 - 成功场景
     */
    @Test
    void testUploadImage_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        UploadResponse mockResponse = UploadResponse.builder()
            .imageUrl("https://pixiv-platform.oss-cn-hangzhou.aliyuncs.com/images/20240101/test-uuid.jpg")
            .thumbnailUrl("https://pixiv-platform.oss-cn-hangzhou.aliyuncs.com/thumbnails/20240101/test-uuid.jpg")
            .fileName("20240101/test-uuid.jpg")
            .fileSize(18L)
            .contentType(MediaType.IMAGE_JPEG_VALUE)
            .build();

        when(fileStorageService.uploadImage(any())).thenReturn(mockResponse);

        // 执行请求并验证响应
        mockMvc.perform(multipart("/api/files/upload")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("操作成功"))
            .andExpect(jsonPath("$.data.imageUrl").value(mockResponse.getImageUrl()))
            .andExpect(jsonPath("$.data.thumbnailUrl").value(mockResponse.getThumbnailUrl()))
            .andExpect(jsonPath("$.data.fileName").value(mockResponse.getFileName()))
            .andExpect(jsonPath("$.data.fileSize").value(mockResponse.getFileSize()))
            .andExpect(jsonPath("$.data.contentType").value(mockResponse.getContentType()));
    }

    /**
     * 测试上传图片接口 - 文件为空
     */
    @Test
    void testUploadImage_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            new byte[0]
        );

        when(fileStorageService.uploadImage(any()))
            .thenThrow(new IllegalArgumentException("文件不能为空"));

        mockMvc.perform(multipart("/api/files/upload")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("文件不能为空"));
    }

    /**
     * 测试健康检查接口
     */
    @Test
    void testHealth() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/files/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value("文件服务运行正常"));
    }
}
