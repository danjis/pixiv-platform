package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.dto.Result;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.ArtistDTO;
import com.pixiv.user.dto.AuthResponse;
import com.pixiv.user.dto.RegisterRequest;
import com.pixiv.user.dto.UserDTO;
import com.pixiv.user.entity.Artist;
import com.pixiv.user.entity.User;
import com.pixiv.user.entity.UserRole;
import com.pixiv.user.feign.ArtworkServiceClient;
import com.pixiv.user.repository.ArtistRepository;
import com.pixiv.user.repository.FollowRepository;
import com.pixiv.user.repository.UserRepository;
import com.pixiv.user.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 * 处理用户注册、登录等业务逻辑
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;
    private final ArtworkServiceClient artworkServiceClient;
    private final MembershipService membershipService;

    public UserService(UserRepository userRepository,
            ArtistRepository artistRepository,
            FollowRepository followRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            CaptchaService captchaService,
            ArtworkServiceClient artworkServiceClient,
            MembershipService membershipService) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.captchaService = captchaService;
        this.artworkServiceClient = artworkServiceClient;
        this.membershipService = membershipService;
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 认证响应（包含令牌和用户信息）
     */
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        logger.info("用户注册: username={}, email={}", request.getUsername(), request.getEmail());

        // 1. 验证邮箱验证码
        if (!captchaService.verifyEmailCode(request.getEmail(), request.getEmailCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱验证码错误或已过期");
        }

        // 2. 验证用户名唯一性
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 3. 验证邮箱唯一性
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 4. 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        // 5. 保存到数据库
        user = userRepository.save(user);
        logger.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        // 6. 生成 JWT 令牌
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername());

        // 7. 返回认证响应
        UserDTO userDTO = new UserDTO(user);
        return new AuthResponse(accessToken, refreshToken, 7200L, userDTO);
    }

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 根据用户ID查找用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 批量根据用户 ID 获取用户基本信息（内部服务调用）
     * 
     * 用于其他微服务批量查询用户信息，避免 N+1 问题。
     * 返回 common 模块的 UserDTO 列表，仅包含基本信息。
     *
     * @param userIds 用户 ID 列表
     * @return 用户基本信息列表
     */
    @Transactional(readOnly = true)
    public java.util.List<com.pixiv.common.dto.UserDTO> getUsersByIds(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(user -> {
                    com.pixiv.common.dto.UserDTO dto = new com.pixiv.common.dto.UserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    dto.setBio(user.getBio());
                    dto.setRole(user.getRole().name());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 用户登录
     *
     * @param usernameOrEmail 用户名或邮箱
     * @param password        密码
     * @return 认证响应（包含令牌和用户信息）
     */
    @Transactional(readOnly = true)
    public AuthResponse login(String usernameOrEmail, String password) {
        logger.info("用户登录: usernameOrEmail={}", usernameOrEmail);

        // 1. 查找用户（支持用户名或邮箱登录）
        User user;
        if (usernameOrEmail.contains("@")) {
            // 邮箱登录
            user = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR));
        } else {
            // 用户名登录
            user = userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR));
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }

        logger.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 3. 生成 JWT 令牌
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername());

        // 4. 返回认证响应
        UserDTO userDTO = new UserDTO(user);
        return new AuthResponse(accessToken, refreshToken, 7200L, userDTO);
    }

    /**
     * 邮箱验证码登录
     *
     * @param email     邮箱地址
     * @param emailCode 邮箱验证码
     * @return 认证响应（包含令牌和用户信息）
     */
    @Transactional(readOnly = true)
    public AuthResponse loginByEmail(String email, String emailCode) {
        logger.info("邮箱验证码登录: email={}", email);

        // 1. 验证邮箱验证码
        if (!captchaService.verifyEmailCode(email, emailCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱验证码错误或已过期");
        }

        // 2. 查找用户
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "该邮箱未注册"));

        logger.info("邮箱验证码登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 3. 生成 JWT 令牌
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername());

        // 4. 返回认证响应
        UserDTO userDTO = new UserDTO(user);
        return new AuthResponse(accessToken, refreshToken, 7200L, userDTO);
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 认证响应（包含新的访问令牌）
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        logger.info("刷新访问令牌");

        // 1. 验证刷新令牌并生成新的访问令牌
        String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
        if (newAccessToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "刷新令牌无效或已过期");
        }

        // 2. 获取用户信息
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = findById(userId);

        logger.info("访问令牌刷新成功: userId={}, username={}", user.getId(), user.getUsername());

        // 3. 返回认证响应（保持原刷新令牌不变）
        UserDTO userDTO = new UserDTO(user);
        return new AuthResponse(newAccessToken, refreshToken, 7200L, userDTO);
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    public void logout(Long userId) {
        logger.info("用户登出: userId={}", userId);

        // 撤销刷新令牌
        jwtTokenProvider.revokeRefreshToken(userId);

        logger.info("用户登出成功: userId={}", userId);
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息 DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Long userId) {
        logger.info("获取当前用户信息: userId={}", userId);

        User user = findById(userId);
        UserDTO userDTO = new UserDTO(user);

        // 查询统计数据
        // 1. 关注数（所有用户都有）
        long followingCount = followRepository.countByFollowerId(userId);
        userDTO.setFollowingCount(followingCount);

        // 2. 如果是画师，查询粉丝数和作品数
        if (user.getRole() == UserRole.ARTIST) {
            // 粉丝数
            long followerCount = followRepository.countByFollowingId(userId);
            userDTO.setFollowerCount(followerCount);

            // 接稿状态
            try {
                artistRepository.findByUserId(userId)
                        .ifPresent(artist -> userDTO.setAcceptingCommissions(artist.getAcceptingCommissions()));
            } catch (Exception e) {
                logger.warn("获取画师接稿状态失败: userId={}", userId, e);
            }

            // 作品数：调用 artwork-service 获取
            try {
                Result<Long> artworkCountResult = artworkServiceClient.getArtworkCount(userId);
                if (artworkCountResult != null && artworkCountResult.getCode() == 200) {
                    userDTO.setArtworkCount(artworkCountResult.getData());
                } else {
                    logger.warn("获取作品数量失败: userId={}, result={}", userId, artworkCountResult);
                    userDTO.setArtworkCount(0L);
                }
            } catch (Exception e) {
                logger.error("调用 artwork-service 获取作品数量失败: userId={}", userId, e);
                userDTO.setArtworkCount(0L);
            }
        }

        // 查询会员等级
        try {
            userDTO.setMembershipLevel(membershipService.getEffectiveLevel(userId).name());
        } catch (Exception e) {
            logger.warn("获取会员等级失败: userId={}", userId, e);
            userDTO.setMembershipLevel("NORMAL");
        }

        logger.info("用户信息查询成功: userId={}, followingCount={}, followerCount={}, artworkCount={}",
                userId, userDTO.getFollowingCount(), userDTO.getFollowerCount(), userDTO.getArtworkCount());

        return userDTO;
    }

    /**
     * 更新用户个人信息
     *
     * @param userId    用户ID
     * @param avatarUrl 头像 URL（可选）
     * @param bio       个人简介（可选）
     * @return 更新后的用户信息 DTO
     */
    @Caching(evict = {
            @CacheEvict(value = "userProfile", key = "#p0"),
            @CacheEvict(value = "artistProfile", key = "#p0")
    })
    @Transactional
    public UserDTO updateProfile(Long userId, String avatarUrl, String bio) {
        logger.info("更新用户个人信息: userId={}, avatarUrl={}, bio={}", userId, avatarUrl, bio);

        // 1. 查找用户
        User user = findById(userId);

        // 2. 更新字段（只更新非空字段）
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        // 3. 保存到数据库
        user = userRepository.save(user);

        logger.info("用户个人信息更新成功: userId={}", userId);

        // 4. 返回更新后的用户信息
        return new UserDTO(user);
    }

    /**
     * 根据用户 ID 获取画师信息
     *
     * @param userId 用户 ID
     * @return 画师信息 DTO
     */
    @Cacheable(value = "artistProfile", key = "#p0")
    @Transactional(readOnly = true)
    public ArtistDTO getArtistByUserId(Long userId) {
        logger.info("获取画师信息: userId={}", userId);

        // 1. 查找画师
        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "画师不存在"));

        logger.info("画师信息查询成功: artistId={}, userId={}, acceptingCommissions={}",
                artist.getId(), userId, artist.getAcceptingCommissions());

        // 2. 返回画师信息
        return new ArtistDTO(artist);
    }

    /**
     * 更新用户隐私设置
     */
    @CacheEvict(value = "userProfile", key = "#p0")
    @Transactional
    public UserDTO updatePrivacySettings(Long userId, java.util.Map<String, Boolean> settings) {
        logger.info("更新隐私设置: userId={}", userId);

        User user = findById(userId);

        if (settings.containsKey("hideFollowing")) {
            user.setHideFollowing(settings.get("hideFollowing"));
        }
        if (settings.containsKey("hideFavorites")) {
            user.setHideFavorites(settings.get("hideFavorites"));
        }

        user = userRepository.save(user);
        logger.info("隐私设置更新成功: userId={}", userId);

        return getCurrentUser(userId);
    }

    /**
     * 更新画师设置（接稿状态、擅长风格、价格区间、联系方式等）
     *
     * @param userId   用户ID
     * @param settings 画师设置参数
     * @return 更新后的画师信息
     */
    @Caching(evict = {
            @CacheEvict(value = "userProfile", key = "#p0"),
            @CacheEvict(value = "artistProfile", key = "#p0")
    })
    @Transactional
    public ArtistDTO updateArtistSettings(Long userId, java.util.Map<String, Object> settings) {
        logger.info("更新画师设置: userId={}, settings={}", userId, settings);

        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "画师不存在"));

        // 更新接稿状态
        if (settings.containsKey("commissionOpen")) {
            artist.setAcceptingCommissions(Boolean.valueOf(settings.get("commissionOpen").toString()));
        }

        // 更新擅长风格（前端传 List，存储为 JSON 字符串）
        if (settings.containsKey("specialties")) {
            try {
                Object specialtiesObj = settings.get("specialties");
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(specialtiesObj);
                artist.setSpecialties(json);
            } catch (Exception e) {
                logger.warn("序列化 specialties 失败", e);
                artist.setSpecialties("[]");
            }
        }

        // 更新价格区间
        if (settings.containsKey("minPrice")) {
            Object v = settings.get("minPrice");
            artist.setMinPrice(v != null ? new java.math.BigDecimal(v.toString()) : null);
        }
        if (settings.containsKey("maxPrice")) {
            Object v = settings.get("maxPrice");
            artist.setMaxPrice(v != null ? new java.math.BigDecimal(v.toString()) : null);
        }

        // 更新联系偏好
        if (settings.containsKey("contactPreference")) {
            Object v = settings.get("contactPreference");
            artist.setContactPreference(v != null ? v.toString() : "platform");
        }
        if (settings.containsKey("contactInfo")) {
            Object v = settings.get("contactInfo");
            artist.setContactInfo(v != null ? v.toString() : "");
        }

        // 更新简介（同步到 User.bio）
        if (settings.containsKey("bio")) {
            Object v = settings.get("bio");
            String bio = v != null ? v.toString() : "";
            artist.getUser().setBio(bio);
            userRepository.save(artist.getUser());
        }

        artistRepository.save(artist);
        logger.info("画师设置更新成功: userId={}", userId);

        return new ArtistDTO(artist);
    }

}
