package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.AuthResponse;
import com.pixiv.user.dto.RegisterRequest;
import com.pixiv.user.entity.User;
import com.pixiv.user.entity.UserRole;
import com.pixiv.user.repository.UserRepository;
import com.pixiv.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(UserRole.USER);
    }

    @Test
    void testRegisterUser_Success() {
        // 模拟行为
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");

        // 执行测试
        AuthResponse response = userService.registerUser(registerRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        // 验证方法调用
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_UsernameExists() {
        // 模拟用户名已存在
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), exception.getCode());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailExists() {
        // 模拟邮箱已存在
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertEquals(ErrorCode.EMAIL_EXISTS.getCode(), exception.getCode());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // 模拟行为
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");

        // 执行测试
        AuthResponse response = userService.login("testuser", "password123");

        // 验证结果
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        // 验证方法调用
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword");
    }

    @Test
    void testLogin_WrongPassword() {
        // 模拟行为
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        assertEquals(ErrorCode.USERNAME_OR_PASSWORD_ERROR.getCode(), exception.getCode());
        verify(passwordEncoder, times(1)).matches("wrongpassword", "hashedPassword");
    }

    @Test
    void testLogin_UserNotFound() {
        // 模拟用户不存在
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("nonexistent", "password123");
        });

        assertEquals(ErrorCode.USERNAME_OR_PASSWORD_ERROR.getCode(), exception.getCode());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}
