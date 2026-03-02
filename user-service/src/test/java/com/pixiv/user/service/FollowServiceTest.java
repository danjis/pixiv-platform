package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.entity.Follow;
import com.pixiv.user.entity.User;
import com.pixiv.user.entity.UserRole;
import com.pixiv.user.feign.ArtworkServiceClient;
import com.pixiv.user.repository.ArtistRepository;
import com.pixiv.user.repository.FollowRepository;
import com.pixiv.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FollowService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtworkServiceClient artworkServiceClient;

    @InjectMocks
    private FollowService followService;

    private User artistUser;

    @BeforeEach
    void setUp() {
        artistUser = new User();
        artistUser.setId(2L);
        artistUser.setUsername("artist");
        artistUser.setRole(UserRole.ARTIST);
    }

    @Nested
    @DisplayName("followUser 关注测试")
    class FollowUserTests {

        @Test
        @DisplayName("成功关注画师")
        void shouldFollowSuccessfully() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(artistUser));
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
            when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArgument(0));

            assertDoesNotThrow(() -> followService.followUser(1L, 2L));

            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("不能关注自己")
        void shouldNotFollowSelf() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> followService.followUser(1L, 1L));

            assertEquals(ErrorCode.CANNOT_FOLLOW_SELF.getCode(), ex.getCode());
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("不能重复关注")
        void shouldNotFollowTwice() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(artistUser));
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> followService.followUser(1L, 2L));

            assertEquals(ErrorCode.ALREADY_FOLLOWING.getCode(), ex.getCode());
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("关注不存在的用户应抛出异常")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> followService.followUser(1L, 999L));
        }
    }

    @Nested
    @DisplayName("unfollowUser 取消关注测试")
    class UnfollowUserTests {

        @Test
        @DisplayName("成功取消关注")
        void shouldUnfollowSuccessfully() {
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

            assertDoesNotThrow(() -> followService.unfollowUser(1L, 2L));

            verify(followRepository).deleteByFollowerIdAndFollowingId(1L, 2L);
        }

        @Test
        @DisplayName("取消未关注的应抛出异常")
        void shouldThrowWhenNotFollowing() {
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> followService.unfollowUser(1L, 2L));

            assertEquals(ErrorCode.NOT_FOLLOWING.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("查询测试")
    class QueryTests {

        @Test
        @DisplayName("获取关注数量")
        void shouldReturnFollowingCount() {
            when(followRepository.countByFollowerId(1L)).thenReturn(10L);

            long count = followService.getFollowingCount(1L);

            assertEquals(10L, count);
        }

        @Test
        @DisplayName("获取粉丝数量")
        void shouldReturnFollowerCount() {
            when(followRepository.countByFollowingId(2L)).thenReturn(100L);

            long count = followService.getFollowerCount(2L);

            assertEquals(100L, count);
        }

        @Test
        @DisplayName("获取关注状态 - 已关注")
        void shouldReturnTrueWhenFollowing() {
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

            assertTrue(followService.checkFollowStatus(1L, 2L));
        }

        @Test
        @DisplayName("获取关注状态 - 未关注")
        void shouldReturnFalseWhenNotFollowing() {
            when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

            assertFalse(followService.checkFollowStatus(1L, 2L));
        }

        @Test
        @DisplayName("获取关注ID列表")
        void shouldReturnFollowingIds() {
            when(followRepository.findFollowingIdsByFollowerId(1L)).thenReturn(List.of(2L, 3L, 4L));

            List<Long> ids = followService.getFollowingIds(1L);

            assertEquals(3, ids.size());
            assertTrue(ids.containsAll(List.of(2L, 3L, 4L)));
        }

        @Test
        @DisplayName("获取粉丝ID列表")
        void shouldReturnFollowerIds() {
            when(followRepository.findFollowerIdsByFollowingId(2L)).thenReturn(List.of(1L, 5L));

            List<Long> ids = followService.getFollowerIds(2L);

            assertEquals(2, ids.size());
            assertTrue(ids.containsAll(List.of(1L, 5L)));
        }
    }
}
