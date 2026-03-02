package com.pixiv.artwork.service;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.Tag;
import com.pixiv.artwork.feign.UserServiceClient;
import com.pixiv.artwork.repository.ArtworkImageRepository;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.ArtworkTagRepository;
import com.pixiv.artwork.repository.FavoriteRepository;
import com.pixiv.artwork.repository.TagRepository;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ArtworkService 单元测试
 * 使用 Mockito 模拟所有依赖，纯单元测试不需要 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class ArtworkServiceTest {

    @Mock
    private ArtworkRepository artworkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArtworkTagRepository artworkTagRepository;

    @Mock
    private ArtworkImageRepository artworkImageRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private TaggingQueueService taggingQueueService;

    @Mock
    private ArtworkInteractionService artworkInteractionService;

    @Mock
    private RankingService rankingService;

    @Mock
    private ViewCountService viewCountService;

    @Mock
    private FeedService feedService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ArtworkService artworkService;

    private Artwork testArtwork;

    @BeforeEach
    void setUp() {
        testArtwork = new Artwork();
        testArtwork.setId(1L);
        testArtwork.setArtistId(100L);
        testArtwork.setArtistName("TestArtist");
        testArtwork.setArtistAvatar("avatar.jpg");
        testArtwork.setTitle("测试作品");
        testArtwork.setDescription("测试描述");
        testArtwork.setImageUrl("image.jpg");
        testArtwork.setThumbnailUrl("thumb.jpg");
        testArtwork.setViewCount(10);
        testArtwork.setLikeCount(5);
        testArtwork.setFavoriteCount(3);
        testArtwork.setCommentCount(1);
        testArtwork.setHotnessScore(50.0);
        testArtwork.setStatus(ArtworkStatus.PUBLISHED);
        testArtwork.setIsAigc(false);
        testArtwork.setCreatedAt(LocalDateTime.now());
        testArtwork.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getArtworkById 测试")
    class GetArtworkByIdTests {

        @Test
        @DisplayName("成功获取作品")
        void shouldReturnArtworkWhenExists() {
            when(artworkRepository.findById(1L)).thenReturn(Optional.of(testArtwork));
            when(artworkTagRepository.findByArtworkId(1L)).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(1L)).thenReturn(Collections.emptyList());

            ArtworkDTO result = artworkService.getArtworkById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("测试作品", result.getTitle());
            assertEquals("TestArtist", result.getArtistName());
            verify(artworkRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("作品不存在应抛出异常")
        void shouldThrowWhenNotExists() {
            when(artworkRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> artworkService.getArtworkById(999L));
            verify(artworkRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("getArtworks 列表查询测试")
    class GetArtworksTests {

        @Test
        @DisplayName("无筛选条件 - 按最新排序")
        void shouldReturnLatestArtworks() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findByStatusOrderByCreatedAtDesc(eq(ArtworkStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(null, null, "latest", 1, 20);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
        }

        @Test
        @DisplayName("关键词搜索")
        void shouldSearchByKeyword() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.searchByKeyword(eq("测试"), eq(ArtworkStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks("测试", null, "latest", 1, 20);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(artworkRepository).searchByKeyword(eq("测试"), eq(ArtworkStatus.PUBLISHED), any(Pageable.class));
        }

        @Test
        @DisplayName("按画师ID筛选")
        void shouldFilterByArtistId() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findByArtistIdAndStatus(eq(100L), eq(ArtworkStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(null, null, "latest", 1, 20, 100L);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(artworkRepository).findByArtistIdAndStatus(eq(100L), eq(ArtworkStatus.PUBLISHED),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("按标签筛选 - 无匹配标签返回空")
        void shouldReturnEmptyWhenNoTagsMatch() {
            when(tagRepository.findByNameIn(anyList())).thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(null, List.of("不存在标签"), "latest", 1, 20);

            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("按标签筛选 - 有匹配")
        void shouldFilterByTags() {
            Tag tag = new Tag();
            tag.setId(10L);
            tag.setName("动漫");
            when(tagRepository.findByNameIn(List.of("动漫"))).thenReturn(List.of(tag));
            when(artworkTagRepository.findArtworkIdsByTagIds(List.of(10L), 1)).thenReturn(List.of(1L));

            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findByIdInAndStatus(eq(List.of(1L)), eq(ArtworkStatus.PUBLISHED),
                    any(Pageable.class)))
                    .thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(null, List.of("动漫"), "latest", 1, 20);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("空结果集")
        void shouldReturnEmptyPageResult() {
            Page<Artwork> emptyPage = new PageImpl<>(Collections.emptyList());
            when(artworkRepository.findByStatusOrderByCreatedAtDesc(eq(ArtworkStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResult<ArtworkDTO> result = artworkService.getArtworks(null, null, "latest", 1, 20);

            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
            assertEquals(0L, result.getTotal());
        }
    }

    @Nested
    @DisplayName("高级搜索测试")
    class AdvancedSearchTests {

        @Test
        @DisplayName("AIGC 筛选使用 Specification")
        @SuppressWarnings("unchecked")
        void shouldUseSpecificationForAigcFilter() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(
                    null, null, "latest", 1, 20, null, true, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(artworkRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("时间范围筛选使用 Specification")
        @SuppressWarnings("unchecked")
        void shouldUseSpecificationForDateRange() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(
                    null, null, "latest", 1, 20, null, null, "2024-01-01", "2024-12-31", null);

            assertNotNull(result);
            verify(artworkRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("最低点赞数筛选使用 Specification")
        @SuppressWarnings("unchecked")
        void shouldUseSpecificationForMinLikes() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(
                    null, null, "latest", 1, 20, null, null, null, null, 50);

            assertNotNull(result);
            verify(artworkRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("组合筛选：关键词 + AIGC + 时间范围")
        @SuppressWarnings("unchecked")
        void shouldCombineMultipleFilters() {
            Page<Artwork> page = new PageImpl<>(List.of(testArtwork));
            when(artworkRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(artworkTagRepository.findByArtworkId(anyLong())).thenReturn(Collections.emptyList());
            when(artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(
                    "风景", null, "hottest", 1, 20, null, false, "2024-06-01", "2024-12-31", 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(artworkRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("高级搜索 + 标签过滤：标签无结果返回空")
        void shouldReturnEmptyWhenTagFilterHasNoResults() {
            when(tagRepository.findByNameIn(List.of("不存在标签"))).thenReturn(Collections.emptyList());

            PageResult<ArtworkDTO> result = artworkService.getArtworks(
                    null, List.of("不存在标签"), "latest", 1, 20, null, true, null, null, null);

            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
            assertEquals(0L, result.getTotal());
        }
    }

    @Nested
    @DisplayName("deleteArtwork 测试")
    class DeleteArtworkTests {

        @Test
        @DisplayName("成功删除作品")
        void shouldDeleteArtwork() {
            when(artworkRepository.findById(1L)).thenReturn(Optional.of(testArtwork));

            assertDoesNotThrow(() -> artworkService.deleteArtwork(100L, 1L));

            verify(artworkRepository).delete(testArtwork);
        }

        @Test
        @DisplayName("删除不存在的作品应抛出异常")
        void shouldThrowWhenDeletingNonExistent() {
            when(artworkRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> artworkService.deleteArtwork(100L, 999L));
        }
    }

    @Nested
    @DisplayName("getArtworkCountByArtist 测试")
    class ArtworkCountTests {

        @Test
        @DisplayName("返回画师作品数量")
        void shouldReturnArtistArtworkCount() {
            when(artworkRepository.countByArtistIdAndStatus(100L, ArtworkStatus.PUBLISHED)).thenReturn(15L);

            long count = artworkService.getArtworkCountByArtist(100L);

            assertEquals(15L, count);
            verify(artworkRepository).countByArtistIdAndStatus(100L, ArtworkStatus.PUBLISHED);
        }

        @Test
        @DisplayName("新画师作品数量为0")
        void shouldReturnZeroForNewArtist() {
            when(artworkRepository.countByArtistIdAndStatus(999L, ArtworkStatus.PUBLISHED)).thenReturn(0L);

            long count = artworkService.getArtworkCountByArtist(999L);

            assertEquals(0L, count);
        }
    }
}
