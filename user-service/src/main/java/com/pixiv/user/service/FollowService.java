package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.dto.Result;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.ArtistDTO;
import com.pixiv.user.dto.UserDTO;
import com.pixiv.user.entity.Artist;
import com.pixiv.user.entity.Follow;
import com.pixiv.user.entity.User;
import com.pixiv.user.entity.UserRole;
import com.pixiv.user.feign.ArtworkServiceClient;
import com.pixiv.user.repository.ArtistRepository;
import com.pixiv.user.repository.FollowRepository;
import com.pixiv.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务
 * 处理用户关注相关的业务逻辑
 */
@Service
public class FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final ArtworkServiceClient artworkServiceClient;

    public FollowService(FollowRepository followRepository,
            UserRepository userRepository,
            ArtistRepository artistRepository,
            ArtworkServiceClient artworkServiceClient) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.artworkServiceClient = artworkServiceClient;
    }

    /**
     * 关注画师
     *
     * @param followerId 关注者ID
     * @param artistId   画师ID（用户ID）
     */
    @Caching(evict = {
            @CacheEvict(value = "followCount", key = "'following:' + #p0"),
            @CacheEvict(value = "followCount", key = "'follower:' + #p1")
    })
    @Transactional
    public void followUser(Long followerId, Long artistId) {
        logger.info("关注画师: followerId={}, artistId={}", followerId, artistId);

        // 1. 验证不能关注自己
        if (followerId.equals(artistId)) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 2. 验证目标用户存在
        User targetUser = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 检查是否已关注
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, artistId)) {
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWING);
        }

        // 4. 创建关注记录
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(artistId);
        followRepository.save(follow);

        // 5. 如果目标用户是画师，增加画师粉丝计数
        if (targetUser.getRole() == UserRole.ARTIST) {
            artistRepository.findByUser(targetUser).ifPresent(artist -> {
                artist.setFollowerCount(artist.getFollowerCount() + 1);
                artistRepository.save(artist);
            });
        }

        logger.info("关注成功: followerId={}, artistId={}", followerId, artistId);
    }

    /**
     * 取消关注画师
     *
     * @param followerId 关注者ID
     * @param artistId   画师ID（用户ID）
     */
    @Caching(evict = {
            @CacheEvict(value = "followCount", key = "'following:' + #p0"),
            @CacheEvict(value = "followCount", key = "'follower:' + #p1")
    })
    @Transactional
    public void unfollowUser(Long followerId, Long artistId) {
        logger.info("取消关注画师: followerId={}, artistId={}", followerId, artistId);

        // 1. 检查是否已关注
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, artistId)) {
            throw new BusinessException(ErrorCode.NOT_FOLLOWING);
        }

        // 2. 删除关注记录
        followRepository.deleteByFollowerIdAndFollowingId(followerId, artistId);

        // 3. 如果目标用户是画师，减少画师粉丝计数
        User targetUser = userRepository.findById(artistId).orElse(null);
        if (targetUser != null && targetUser.getRole() == UserRole.ARTIST) {
            artistRepository.findByUser(targetUser).ifPresent(artist -> {
                artist.setFollowerCount(Math.max(0, artist.getFollowerCount() - 1));
                artistRepository.save(artist);
            });
        }

        logger.info("取消关注成功: followerId={}, artistId={}", followerId, artistId);
    }

    /**
     * 获取关注列表（我关注的人）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 关注的用户列表
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowing(Long userId, Pageable pageable) {
        logger.info("获取关注列表: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 查询关注关系
        Page<Follow> followPage = followRepository.findByFollowerId(userId, pageable);

        // 2. 获取被关注用户的ID列表
        List<Long> followingIds = followPage.getContent().stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());

        // 3. 查询用户信息
        List<User> users = userRepository.findAllById(followingIds);

        // 4. 转换为 DTO
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, followPage.getTotalElements());
    }

    /**
     * 获取粉丝列表（关注我的人）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 粉丝列表
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowers(Long userId, Pageable pageable) {
        logger.info("获取粉丝列表: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 查询关注关系
        Page<Follow> followPage = followRepository.findByFollowingId(userId, pageable);

        // 2. 获取关注者的ID列表
        List<Long> followerIds = followPage.getContent().stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());

        // 3. 查询用户信息
        List<User> users = userRepository.findAllById(followerIds);

        // 4. 转换为 DTO
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, followPage.getTotalElements());
    }

    /**
     * 检查是否关注
     *
     * @param followerId 关注者ID
     * @param artistId   画师ID（用户ID）
     * @return 是否关注
     */
    @Transactional(readOnly = true)
    public boolean checkFollowStatus(Long followerId, Long artistId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, artistId);
    }

    /**
     * 获取关注列表（带统计数据：作品数、粉丝数）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 关注的用户列表（含统计数据）
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowingWithStats(Long userId, Pageable pageable) {
        Page<UserDTO> page = getFollowing(userId, pageable);
        enrichUserStats(page.getContent());
        return page;
    }

    /**
     * 获取粉丝列表（带统计数据：作品数、粉丝数）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 粉丝列表（含统计数据）
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowersWithStats(Long userId, Pageable pageable) {
        Page<UserDTO> page = getFollowers(userId, pageable);
        enrichUserStats(page.getContent());
        return page;
    }

    /**
     * 为用户列表填充统计数据（作品数、粉丝数）
     */
    private void enrichUserStats(List<UserDTO> userDTOs) {
        for (UserDTO dto : userDTOs) {
            // 粉丝数
            long followerCount = followRepository.countByFollowingId(dto.getId());
            dto.setFollowerCount(followerCount);

            // 作品数：通过 Feign 调用 artwork-service 获取准确数量
            if ("ARTIST".equals(dto.getRole())) {
                try {
                    Result<Long> artworkCountResult = artworkServiceClient.getArtworkCount(dto.getId());
                    if (artworkCountResult != null && artworkCountResult.getCode() == 200) {
                        dto.setArtworkCount(artworkCountResult.getData());
                    } else {
                        dto.setArtworkCount(0L);
                    }
                } catch (Exception e) {
                    logger.warn("获取用户 {} 作品数失败: {}", dto.getId(), e.getMessage());
                    dto.setArtworkCount(0L);
                }
            }
            if (dto.getArtworkCount() == null) {
                dto.setArtworkCount(0L);
            }
        }
    }

    /**
     * 检查用户是否隐藏了关注列表
     */
    public boolean isFollowingHidden(Long userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getHideFollowing()))
                .orElse(false);
    }

    /**
     * 检查用户是否隐藏了收藏列表
     */
    public boolean isFavoritesHidden(Long userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getHideFavorites()))
                .orElse(false);
    }

    /**
     * 获取用户关注的所有人的 ID 列表
     *
     * @param userId 用户ID
     * @return 关注的用户 ID 列表
     */
    @Transactional(readOnly = true)
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIdsByFollowerId(userId);
    }

    /**
     * 获取用户的所有粉丝 ID 列表
     *
     * @param userId 用户ID
     * @return 粉丝 ID 列表
     */
    @Transactional(readOnly = true)
    public List<Long> getFollowerIds(Long userId) {
        return followRepository.findFollowerIdsByFollowingId(userId);
    }

    /**
     * 获取关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    @Cacheable(value = "followCount", key = "'following:' + #p0")
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    /**
     * 获取粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    @Cacheable(value = "followCount", key = "'follower:' + #p0")
    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }
}
