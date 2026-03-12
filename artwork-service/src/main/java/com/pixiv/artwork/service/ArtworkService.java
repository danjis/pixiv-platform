package com.pixiv.artwork.service;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.dto.ArtworkDetailDTO;
import com.pixiv.artwork.dto.CreateArtworkRequest;
import com.pixiv.artwork.dto.UpdateArtworkRequest;
import com.pixiv.artwork.dto.TagDTO;
import com.pixiv.artwork.dto.ImageDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.ArtworkTag;
import com.pixiv.artwork.entity.Tag;
import com.pixiv.artwork.entity.TagSource;
import com.pixiv.artwork.entity.ArtworkImage;
import com.pixiv.artwork.feign.FileServiceClient;
import com.pixiv.artwork.feign.UserServiceClient;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.ArtworkTagRepository;
import com.pixiv.artwork.repository.ArtworkImageRepository;
import com.pixiv.artwork.repository.FavoriteRepository;
import com.pixiv.artwork.repository.TagRepository;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.common.exception.ResourceNotFoundException;
import com.pixiv.common.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 作品服务类
 * 
 * 处理作品相关的业务逻辑
 */
@Service
public class ArtworkService {

    private static final Logger logger = LoggerFactory.getLogger(ArtworkService.class);

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArtworkTagRepository artworkTagRepository;

    @Autowired
    private ArtworkImageRepository artworkImageRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private FileServiceClient fileServiceClient;

    @Autowired
    private TaggingQueueService taggingQueueService;

    @Autowired
    private ArtworkInteractionService artworkInteractionService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ViewCountService viewCountService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTWORK_CACHE_PREFIX = "artwork:";
    private static final long ARTWORK_CACHE_TTL = 1; // 缓存 1 小时

    /**
     * 创建作品
     * 
     * @param artistId 画师 ID
     * @param request  创建作品请求
     * @return 作品 DTO
     */
    @CacheEvict(value = "artworkCount", key = "#p0")
    @Transactional
    public ArtworkDTO createArtwork(Long artistId, CreateArtworkRequest request) {
        logger.info("开始创建作品: artistId={}, title={}", artistId, request.getTitle());

        // 1. 验证画师身份 - 调用用户服务获取用户信息
        Result<UserDTO> userResult = userServiceClient.getUserById(artistId);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            logger.error("用户信息获取失败: userId={}", artistId);
            throw new BusinessException(com.pixiv.common.constant.ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        UserDTO user = userResult.getData();

        // 检查用户角色是否为 ARTIST
        if (!"ARTIST".equals(user.getRole())) {
            logger.error("用户不是画师: userId={}, role={}", artistId, user.getRole());
            throw new BusinessException(com.pixiv.common.constant.ErrorCode.USER_NOT_ARTIST, "只有画师才能发布作品");
        }

        logger.info("画师身份验证成功: userId={}, username={}", artistId, user.getUsername());

        // 2. 创建作品实体
        Artwork artwork = new Artwork();
        artwork.setArtistId(artistId);
        artwork.setArtistName(user.getUsername()); // 冗余画师名称，避免频繁调用用户服务
        artwork.setArtistAvatar(user.getAvatarUrl()); // 冗余画师头像
        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());

        // 判断是否为草稿
        boolean isDraft = Boolean.TRUE.equals(request.getIsDraft());

        // 处理多图 / 单图兼容
        List<CreateArtworkRequest.ImageItem> imageItems = request.getImages();
        if (imageItems != null && !imageItems.isEmpty()) {
            // 多图模式：第一张作为封面
            artwork.setImageUrl(imageItems.get(0).getImageUrl());
            artwork.setThumbnailUrl(imageItems.get(0).getThumbnailUrl());
            artwork.setImageCount(imageItems.size());
        } else if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            // 单图兼容模式
            artwork.setImageUrl(request.getImageUrl());
            artwork.setThumbnailUrl(request.getThumbnailUrl());
            artwork.setImageCount(1);
        } else if (!isDraft) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请至少上传一张图片");
        } else {
            // 草稿允许无图片
            artwork.setImageUrl("");
            artwork.setImageCount(0);
        }

        artwork.setStatus(isDraft ? ArtworkStatus.DRAFT : ArtworkStatus.PUBLISHED);
        artwork.setIsAigc(request.getIsAigc() != null ? request.getIsAigc() : false);

        // 3. 保存作品到数据库
        artwork = artworkRepository.save(artwork);
        logger.info("{}保存成功: artworkId={}", isDraft ? "草稿" : "作品", artwork.getId());

        // 3.5 保存多图记录
        if (imageItems != null && !imageItems.isEmpty()) {
            for (int i = 0; i < imageItems.size(); i++) {
                ArtworkImage img = new ArtworkImage();
                img.setArtworkId(artwork.getId());
                img.setImageUrl(imageItems.get(i).getImageUrl());
                img.setOriginalImageUrl(imageItems.get(i).getOriginalImageUrl());
                img.setThumbnailUrl(imageItems.get(i).getThumbnailUrl());
                img.setSortOrder(i);
                artworkImageRepository.save(img);
            }
            logger.info("多图记录保存成功: artworkId={}, 图片数={}", artwork.getId(), imageItems.size());
        } else if (artwork.getImageUrl() != null) {
            // 单图也存一条记录，保持一致性
            ArtworkImage img = new ArtworkImage();
            img.setArtworkId(artwork.getId());
            img.setImageUrl(artwork.getImageUrl());
            img.setThumbnailUrl(artwork.getThumbnailUrl());
            img.setSortOrder(0);
            artworkImageRepository.save(img);
        }

        // 4. 处理手动添加的标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveManualTags(artwork.getId(), request.getTags());
            logger.info("手动标签保存成功: artworkId={}, 标签数={}", artwork.getId(), request.getTags().size());
        }

        // 5. 发送到消息队列进行智能打标（仅发布状态且有图片才触发）
        if (!isDraft && artwork.getImageUrl() != null) {
            taggingQueueService.sendTaggingRequest(artwork.getId(), artwork.getImageUrl());
            logger.info("智能打标请求已发送: artworkId={}", artwork.getId());
        }

        // 5.5 推送到粉丝 Feed 收件箱（仅发布状态）
        if (!isDraft) {
            pushToFollowerFeeds(artistId, artwork.getId());
        }

        // 6. 转换为 DTO 并返回
        return convertToDTO(artwork);
    }

    /**
     * 保存手动添加的标签
     * 
     * @param artworkId 作品 ID
     * @param tagNames  标签名称列表
     */
    @Transactional
    public void saveManualTags(Long artworkId, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 查找或创建标签
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        newTag.setUsageCount(0);
                        return tagRepository.save(newTag);
                    });

            // 检查是否已存在关联
            if (!artworkTagRepository.existsByArtworkIdAndTagId(artworkId, tag.getId())) {
                // 创建作品标签关联
                ArtworkTag artworkTag = new ArtworkTag();
                artworkTag.setArtworkId(artworkId);
                artworkTag.setTagId(tag.getId());
                artworkTag.setSource(TagSource.MANUAL); // 标记为手动添加
                artworkTag.setConfidence(null); // 手动标签没有置信度

                artworkTagRepository.save(artworkTag);

                // 增加标签使用计数
                tag.incrementUsageCount();
                tagRepository.save(tag);
            }
        }
    }

    /**
     * 更新作品信息
     * 
     * @param artworkId 作品 ID
     * @param artistId  画师 ID（用于权限验证）
     * @param request   更新请求
     * @return 更新后的作品 DTO
     */
    @Transactional
    public ArtworkDTO updateArtwork(Long artworkId, Long artistId, UpdateArtworkRequest request) {
        logger.info("更新作品: artworkId={}, artistId={}", artworkId, artistId);

        // 1. 查找作品
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: id=" + artworkId));

        // 2. 验证权限：只有作品的创建者才能编辑
        if (!artwork.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权编辑此作品");
        }

        // 3. 更新字段（只更新非空字段）
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            artwork.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            artwork.setDescription(request.getDescription().trim());
        }

        // 3.1 更新草稿/发布状态
        ArtworkStatus oldStatus = artwork.getStatus();
        if (request.getIsDraft() != null) {
            artwork.setStatus(request.getIsDraft() ? ArtworkStatus.DRAFT : ArtworkStatus.PUBLISHED);
        }

        // 3.2 更新图片（如果提供了图片列表）
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // 用第一张图片作为封面
            artwork.setImageUrl(request.getImages().get(0).getImageUrl());
            artwork.setThumbnailUrl(request.getImages().get(0).getThumbnailUrl());

            // 删除旧图片记录，插入新图片
            artworkImageRepository.deleteByArtworkId(artworkId);
            for (int i = 0; i < request.getImages().size(); i++) {
                ArtworkImage img = new ArtworkImage();
                img.setArtworkId(artworkId);
                img.setImageUrl(request.getImages().get(i).getImageUrl());
                img.setOriginalImageUrl(request.getImages().get(i).getOriginalImageUrl());
                img.setThumbnailUrl(request.getImages().get(i).getThumbnailUrl());
                img.setSortOrder(i);
                artworkImageRepository.save(img);
            }
            logger.info("图片更新成功: artworkId={}, 图片数={}", artworkId, request.getImages().size());
        }

        artworkRepository.save(artwork);

        // 4. 更新手动标签（如果提供了标签列表）
        if (request.getTags() != null) {
            // 删除旧的手动标签
            artworkTagRepository.deleteByArtworkIdAndSource(artworkId, TagSource.MANUAL);
            // 保存新的手动标签
            if (!request.getTags().isEmpty()) {
                saveManualTags(artworkId, request.getTags());
            }
            logger.info("手动标签更新成功: artworkId={}, 标签数={}", artworkId, request.getTags().size());
        }

        // 5. 清除缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 5.5 草稿 → 发布：触发智能打标 & 推送粉丝 Feed
        boolean isDraftToPublish = oldStatus == ArtworkStatus.DRAFT
                && artwork.getStatus() == ArtworkStatus.PUBLISHED;
        if (isDraftToPublish) {
            // 智能打标
            if (artwork.getImageUrl() != null && !artwork.getImageUrl().isEmpty()) {
                taggingQueueService.sendTaggingRequest(artwork.getId(), artwork.getImageUrl());
                logger.info("草稿发布 - 智能打标请求已发送: artworkId={}", artwork.getId());
            }
            // 推送到粉丝 Feed
            pushToFollowerFeeds(artistId, artwork.getId());
            logger.info("草稿发布 - 已推送到粉丝 Feed: artworkId={}", artwork.getId());
        }

        logger.info("作品更新成功: artworkId={}", artworkId);
        return convertToDTO(artwork);
    }

    /**
     * 根据 ID 获取作品
     * 
     * @param artworkId 作品 ID
     * @return 作品 DTO
     */
    public ArtworkDTO getArtworkById(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: id=" + artworkId));

        return convertToDTO(artwork);
    }

    /**
     * 获取作品详情（带缓存和浏览计数）
     * 
     * @param artworkId     作品 ID
     * @param currentUserId 当前用户 ID（可选，用于查询点赞收藏状态）
     * @return 作品详情 DTO
     */
    public ArtworkDetailDTO getArtwork(Long artworkId, Long currentUserId) {
        logger.info("获取作品详情: artworkId={}, userId={}", artworkId, currentUserId);

        // 1. 尝试从缓存获取（仅对已发布作品使用缓存）
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        ArtworkDetailDTO cached = (ArtworkDetailDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            logger.info("从缓存获取作品详情: artworkId={}", artworkId);
            // 异步增加浏览计数（委托给独立 Service）
            viewCountService.incrementViewCountAsync(artworkId);
            // 补充 Redis 中尚未刷入 DB 的实时浏览增量（缓存的 viewCount 是 DB 快照）
            int delta = viewCountService.getUnflushedDelta(artworkId);
            if (delta > 0) {
                cached.setViewCount(cached.getViewCount() + delta);
            }
            return cached;
        }

        // 2. 从数据库查询 — 先尝试已发布的作品
        Artwork artwork = artworkRepository.findByIdAndStatus(artworkId, ArtworkStatus.PUBLISHED)
                .orElse(null);

        // 2.1 如果不是已发布作品，检查是否是作者本人查看草稿
        if (artwork == null) {
            if (currentUserId == null) {
                throw new ResourceNotFoundException("作品不存在或未发布: id=" + artworkId);
            }
            artwork = artworkRepository.findById(artworkId)
                    .orElseThrow(() -> new ResourceNotFoundException("作品不存在: id=" + artworkId));
            if (!artwork.getArtistId().equals(currentUserId)) {
                throw new ResourceNotFoundException("作品不存在或未发布: id=" + artworkId);
            }
            logger.info("作者查看自己的草稿: artworkId={}, userId={}", artworkId, currentUserId);
        }

        // 3. 转换为详情 DTO
        ArtworkDetailDTO detailDTO = convertToDetailDTO(artwork);

        // 4. 如果用户已登录，查询点赞和收藏状态
        if (currentUserId != null) {
            boolean isLiked = artworkInteractionService.isLiked(currentUserId, artworkId);
            boolean isFavorited = artworkInteractionService.isFavorited(currentUserId, artworkId);
            detailDTO.setIsLiked(isLiked);
            detailDTO.setIsFavorited(isFavorited);
            logger.info("查询用户互动状态: userId={}, artworkId={}, isLiked={}, isFavorited={}",
                    currentUserId, artworkId, isLiked, isFavorited);
        }

        // 5. 仅对已发布作品存入缓存（草稿不缓存）
        if (artwork.getStatus() == ArtworkStatus.PUBLISHED) {
            redisTemplate.opsForValue().set(cacheKey, detailDTO, ARTWORK_CACHE_TTL, TimeUnit.HOURS);
            logger.info("作品详情已缓存: artworkId={}", artworkId);

            // 6. 异步增加浏览计数（仅已发布作品计数）
            viewCountService.incrementViewCountAsync(artworkId);

            // 6.1 补充实时浏览增量（缓存的 viewCount 是 DB 快照，需要加上 Redis delta）
            int delta = viewCountService.getUnflushedDelta(artworkId);
            if (delta > 0) {
                detailDTO.setViewCount(detailDTO.getViewCount() + delta);
            }

            // 7. 异步记录浏览历史（仅登录用户）
            if (currentUserId != null) {
                browsingHistoryService.recordView(currentUserId, artworkId);
            }
        }

        return detailDTO;
    }

    /**
     * 获取作品列表（支持分页、搜索、筛选、排序）
     */
    public PageResult<ArtworkDTO> getArtworks(String keyword, List<String> tags, String sortBy, int page, int size) {
        return getArtworks(keyword, tags, sortBy, page, size, null, null, null, null, null);
    }

    /**
     * 获取作品列表（兼容旧调用）
     */
    public PageResult<ArtworkDTO> getArtworks(String keyword, List<String> tags, String sortBy, int page, int size,
            Long artistId) {
        return getArtworks(keyword, tags, sortBy, page, size, artistId, null, null, null, null);
    }

    /**
     * 高级搜索：支持关键词、标签、排序、AIGC筛选、时间范围、最低点赞数等多维度组合查询
     *
     * @param keyword  搜索关键词（匹配标题、描述、画师名）
     * @param tags     标签列表（取交集）
     * @param sortBy   排序方式
     * @param page     页码（从 1 开始）
     * @param size     每页大小
     * @param artistId 画师ID
     * @param isAigc   是否AI生成
     * @param dateFrom 开始日期（yyyy-MM-dd）
     * @param dateTo   结束日期（yyyy-MM-dd）
     * @param minLikes 最低点赞数
     * @return 分页作品列表
     */
    public PageResult<ArtworkDTO> getArtworks(String keyword, List<String> tags, String sortBy, int page, int size,
            Long artistId, Boolean isAigc, String dateFrom, String dateTo, Integer minLikes) {
        logger.info(
                "高级搜索: keyword={}, tags={}, sortBy={}, artistId={}, isAigc={}, dateFrom={}, dateTo={}, minLikes={}, page={}, size={}",
                keyword, tags, sortBy, artistId, isAigc, dateFrom, dateTo, minLikes, page, size);

        // 创建分页对象（Spring Data 的页码从 0 开始）
        Pageable pageable = createPageable(sortBy, page - 1, size);

        // 判断是否有高级筛选条件
        boolean hasAdvancedFilter = isAigc != null || dateFrom != null || dateTo != null
                || (minLikes != null && minLikes > 0);

        Page<Artwork> artworkPage;

        if (hasAdvancedFilter || (keyword != null && !keyword.trim().isEmpty() && artistId != null)) {
            // 使用 JPA Specification 进行动态组合查询
            LocalDateTime dateFromParsed = dateFrom != null ? java.time.LocalDate.parse(dateFrom).atStartOfDay() : null;
            LocalDateTime dateToParsed = dateTo != null ? java.time.LocalDate.parse(dateTo).atTime(23, 59, 59) : null;

            // 标签过滤：先查出符合标签条件的作品ID列表
            List<Long> tagFilteredIds = null;
            if (tags != null && !tags.isEmpty()) {
                tagFilteredIds = getArtworkIdsByTags(tags);
                if (tagFilteredIds.isEmpty()) {
                    return new PageResult<>(Collections.emptyList(), 0L, page, size);
                }
            }

            artworkPage = artworkRepository.findAll(
                    com.pixiv.artwork.specification.ArtworkSpecification.advancedSearch(
                            keyword, isAigc, dateFromParsed, dateToParsed, minLikes, tagFilteredIds, artistId),
                    pageable);
        }
        // 简单查询（无高级筛选，走旧逻辑以保持兼容）
        else if (artistId != null) {
            artworkPage = artworkRepository.findByArtistIdAndStatus(artistId, ArtworkStatus.PUBLISHED, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            artworkPage = getArtworksByTags(tags, keyword, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            artworkPage = artworkRepository.searchByKeyword(keyword.trim(), ArtworkStatus.PUBLISHED, pageable);
        } else {
            artworkPage = getArtworksBySortType(sortBy, pageable);
        }

        // 批量转换为 DTO（优化 N+1 查询问题）
        List<ArtworkDTO> artworkDTOs = convertToDTOBatch(artworkPage.getContent());

        logger.info("作品列表查询成功: 总数={}, 当前页={}, 每页={}", artworkPage.getTotalElements(), page, size);

        return new PageResult<>(
                artworkDTOs,
                artworkPage.getTotalElements(),
                page,
                size);
    }

    /**
     * 获取关注动态流（Feed）
     * 查询当前用户关注的画师所发布的作品，按创建时间倒序排列
     *
     * @param userId 当前用户 ID
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 关注画师的作品列表
     */
    public PageResult<ArtworkDTO> getFeedArtworks(Long userId, int page, int size) {
        logger.info("获取关注动态流: userId={}, page={}, size={}", userId, page, size);

        // === 优先从 Redis Feed 收件箱获取（推模式） ===
        List<Long> cachedIds = feedService.getFeedArtworkIds(userId, page, size);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            long total = feedService.getFeedSize(userId);
            List<Artwork> artworks = artworkRepository.findAllById(cachedIds);
            // 保持 Redis 中的排序顺序
            java.util.Map<Long, Artwork> artworkMap = artworks.stream()
                    .collect(Collectors.toMap(Artwork::getId, a -> a));
            List<Artwork> ordered = cachedIds.stream()
                    .map(artworkMap::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            List<ArtworkDTO> dtos = convertToDTOBatch(ordered);
            logger.info("Feed 从 Redis 缓存获取成功: userId={}, 数量={}", userId, dtos.size());
            return new PageResult<>(dtos, total, page, size);
        }

        // === Redis 无数据，降级到拉模式（Feign + DB 查询） ===
        // 1. 通过 Feign 调用 user-service 获取关注列表
        Result<List<Long>> followingResult = userServiceClient.getFollowingIds(userId);

        if (followingResult == null || followingResult.getCode() != 200 || followingResult.getData() == null) {
            logger.warn("获取关注列表失败或为空: userId={}", userId);
            return new PageResult<>(Collections.emptyList(), 0L, page, size);
        }

        List<Long> followingIds = followingResult.getData();
        if (followingIds.isEmpty()) {
            logger.info("用户没有关注任何人: userId={}", userId);
            return new PageResult<>(Collections.emptyList(), 0L, page, size);
        }

        logger.info("用户关注了 {} 个人: userId={}", followingIds.size(), userId);

        // 2. 查询这些画师的作品
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Artwork> artworkPage = artworkRepository.findByArtistIdInAndStatus(
                followingIds, ArtworkStatus.PUBLISHED, pageable);

        // 3. 转换为 DTO
        List<ArtworkDTO> artworkDTOs = convertToDTOBatch(artworkPage.getContent());

        logger.info("关注动态流查询成功: userId={}, 总数={}", userId, artworkPage.getTotalElements());

        return new PageResult<>(artworkDTOs, artworkPage.getTotalElements(), page, size);
    }

    /**
     * 获取排行榜作品
     *
     * @param sortBy 排序方式 (hottest, most_liked, most_favorited, most_viewed)
     * @param period 时间范围 (day, week, month, all)
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 排行榜作品列表
     */
    public PageResult<ArtworkDTO> getRankingArtworks(String sortBy, String period, int page, int size) {
        logger.info("获取排行榜: sortBy={}, period={}, page={}, size={}", sortBy, period, page, size);

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "hottest";
        }

        // === 优先从 Redis ZSet 获取排行榜 ===
        // 所有排序类型都尝试走 Redis（按 sortBy+period 组合查询）
        String rankingKey = rankingService.getRankingKey(sortBy, period);
        if (rankingService.isRankingAvailable(rankingKey)) {
            List<Long> rankedIds = rankingService.getRankingIdsByKey(rankingKey, page, size);
            long total = rankingService.getRankingCountByKey(rankingKey);

            if (!rankedIds.isEmpty()) {
                // 按排行榜顺序从 DB 加载作品详情
                List<Artwork> artworks = artworkRepository.findAllById(rankedIds);
                // 保持 Redis 返回的排名顺序
                java.util.Map<Long, Artwork> artworkMap = artworks.stream()
                        .collect(Collectors.toMap(Artwork::getId, a -> a));
                List<Artwork> ordered = rankedIds.stream()
                        .map(artworkMap::get)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());

                List<ArtworkDTO> dtos = convertToDTOBatch(ordered);
                logger.info("排行榜查询成功(Redis): sortBy={}, period={}, 总数={}, 当前页={}", sortBy, period, total, page);
                return new PageResult<>(dtos, total, page, size);
            }
        }

        // === Redis 不可用时降级到数据库查询 ===
        // 注意：按点赞/收藏/浏览排序时，不按创建时间筛选
        // 因为"今日最多点赞"应包含所有被点赞的作品，而非仅今日创建的作品
        Page<Artwork> artworkPage;
        Pageable sortedPageable = createPageable(sortBy, page - 1, size);
        artworkPage = getArtworksBySortType(sortBy, sortedPageable);

        List<ArtworkDTO> artworkDTOs = convertToDTOBatch(artworkPage.getContent());

        logger.info("排行榜查询成功(DB): 总数={}, 当前页={}", artworkPage.getTotalElements(), page);

        return new PageResult<>(artworkDTOs, artworkPage.getTotalElements(), page, size);
    }

    /**
     * 根据时间范围获取起始时间
     */
    private LocalDateTime getStartTimeByPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "day":
                return now.minusDays(1);
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            default:
                return now.minusDays(1);
        }
    }

    /**
     * 根据标签筛选作品
     * 
     * @param tagNames 标签名称列表
     * @param keyword  搜索关键词（可选）
     * @param pageable 分页对象
     * @return 作品分页结果
     */
    private Page<Artwork> getArtworksByTags(List<String> tagNames, String keyword, Pageable pageable) {
        // 1. 批量查询标签 ID（单次 DB 调用替代循环）
        List<Tag> foundTags = tagRepository.findByNameIn(tagNames);
        List<Long> tagIds = foundTags.stream().map(Tag::getId).collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 查找包含所有指定标签的作品 ID（交集）
        List<Long> artworkIds = artworkTagRepository.findArtworkIdsByTagIds(tagIds, tagIds.size());

        if (artworkIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3. 根据作品 ID 列表查询作品
        if (keyword != null && !keyword.trim().isEmpty()) {
            return artworkRepository.findByIdInAndStatusAndTitleOrDescriptionContaining(
                    artworkIds, ArtworkStatus.PUBLISHED, keyword.trim(), pageable);
        } else {
            return artworkRepository.findByIdInAndStatus(artworkIds, ArtworkStatus.PUBLISHED, pageable);
        }
    }

    /**
     * 根据标签获取作品ID列表（用于 Specification 高级搜索）
     */
    private List<Long> getArtworkIdsByTags(List<String> tagNames) {
        List<Tag> foundTags = tagRepository.findByNameIn(tagNames);
        List<Long> tagIds = foundTags.stream().map(Tag::getId).collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return artworkTagRepository.findArtworkIdsByTagIds(tagIds, tagIds.size());
    }

    /**
     * 根据排序类型获取作品
     * 
     * @param sortBy   排序类型
     * @param pageable 分页对象
     * @return 作品分页结果
     */
    private Page<Artwork> getArtworksBySortType(String sortBy, Pageable pageable) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "latest"; // 默认按最新排序
        }

        switch (sortBy.toLowerCase()) {
            case "hottest":
                return artworkRepository.findByStatusOrderByHotnessScoreDesc(ArtworkStatus.PUBLISHED, pageable);
            case "most_liked":
                return artworkRepository.findByStatusOrderByLikeCountDesc(ArtworkStatus.PUBLISHED, pageable);
            case "most_favorited":
                return artworkRepository.findByStatusOrderByFavoriteCountDesc(ArtworkStatus.PUBLISHED, pageable);
            case "most_viewed":
                return artworkRepository.findByStatusOrderByViewCountDesc(ArtworkStatus.PUBLISHED, pageable);
            case "latest":
            default:
                return artworkRepository.findByStatusOrderByCreatedAtDesc(ArtworkStatus.PUBLISHED, pageable);
        }
    }

    /**
     * 创建分页对象
     * 
     * @param sortBy 排序类型
     * @param page   页码（从 0 开始）
     * @param size   每页大小
     * @return 分页对象
     */
    private Pageable createPageable(String sortBy, int page, int size) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "latest";
        }

        Sort sort;
        switch (sortBy.toLowerCase()) {
            case "hottest":
                sort = Sort.by(Sort.Direction.DESC, "hotnessScore");
                break;
            case "most_liked":
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case "most_favorited":
                sort = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "most_viewed":
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            case "latest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        return PageRequest.of(page, size, sort);
    }

    /**
     * 公开的批量 DTO 转换方法，供 AdminArtworkController 等调用
     */
    public List<ArtworkDTO> toArtworkDTOList(List<Artwork> artworks) {
        return convertToDTOBatch(artworks);
    }

    /**
     * 批量将作品实体转换为 DTO（优化 N+1 查询）
     * 
     * @param artworks 作品实体列表
     * @return 作品 DTO 列表
     */
    private List<ArtworkDTO> convertToDTOBatch(List<Artwork> artworks) {
        if (artworks == null || artworks.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有作品 ID
        List<Long> artworkIds = artworks.stream()
                .map(Artwork::getId)
                .collect(Collectors.toList());

        // 2. 批量查询所有作品的图片
        List<ArtworkImage> allImages = artworkImageRepository.findByArtworkIdInOrderBySortOrderAsc(artworkIds);
        java.util.Map<Long, List<ImageDTO>> artworkImagesMap = allImages.stream()
                .collect(Collectors.groupingBy(
                        ArtworkImage::getArtworkId,
                        Collectors.mapping(
                                img -> new ImageDTO(img.getImageUrl(), img.getThumbnailUrl(), img.getSortOrder()),
                                Collectors.toList())));

        // 3. 批量查询所有作品的标签关联
        List<ArtworkTag> allArtworkTags = artworkTagRepository.findByArtworkIdIn(artworkIds);

        // 4. 收集所有标签 ID
        List<Long> tagIds = allArtworkTags.stream()
                .map(ArtworkTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        // 5. 批量查询所有标签
        List<Tag> allTags = tagIds.isEmpty() ? new ArrayList<>() : tagRepository.findAllById(tagIds);

        // 6. 构建标签 ID 到标签实体的映射
        java.util.Map<Long, Tag> tagMap = allTags.stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));

        // 7. 构建作品 ID 到标签列表的映射
        java.util.Map<Long, List<TagDTO>> artworkTagsMap = allArtworkTags.stream()
                .collect(Collectors.groupingBy(
                        ArtworkTag::getArtworkId,
                        Collectors.mapping(
                                artworkTag -> {
                                    Tag tag = tagMap.get(artworkTag.getTagId());
                                    if (tag == null) {
                                        return null;
                                    }
                                    TagDTO dto = new TagDTO();
                                    dto.setId(tag.getId());
                                    dto.setName(tag.getName());
                                    dto.setSource(artworkTag.getSource());
                                    dto.setConfidence(artworkTag.getConfidence());
                                    return dto;
                                },
                                Collectors.filtering(
                                        tagDTO -> tagDTO != null,
                                        Collectors.toList()))));

        // 8. 对 artistAvatar 为空的作品，批量通过用户服务补全头像（使用批量接口避免 N+1）
        List<Long> artistIdsNeedAvatar = artworks.stream()
                .filter(a -> a.getArtistAvatar() == null || a.getArtistAvatar().isEmpty())
                .map(Artwork::getArtistId)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, String> avatarMap = new java.util.HashMap<>();
        if (!artistIdsNeedAvatar.isEmpty()) {
            try {
                Result<List<UserDTO>> batchResult = userServiceClient.getUsersByIds(artistIdsNeedAvatar);
                if (batchResult != null && batchResult.isSuccess() && batchResult.getData() != null) {
                    for (UserDTO u : batchResult.getData()) {
                        if (u.getAvatarUrl() != null) {
                            avatarMap.put(u.getId(), u.getAvatarUrl());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("批量获取用户头像失败, 降级为逐个查询: {}", e.getMessage());
            }
            // 批量接口未返回全部结果时，逐个补查缺失的用户
            if (avatarMap.size() < artistIdsNeedAvatar.size()) {
                for (Long aid : artistIdsNeedAvatar) {
                    if (avatarMap.containsKey(aid))
                        continue;
                    try {
                        Result<UserDTO> ur = userServiceClient.getUserById(aid);
                        if (ur != null && ur.isSuccess() && ur.getData() != null
                                && ur.getData().getAvatarUrl() != null) {
                            avatarMap.put(aid, ur.getData().getAvatarUrl());
                        }
                    } catch (Exception ex) {
                        logger.warn("获取用户头像失败: userId={}", aid);
                    }
                }
            }
        }

        // 9. 转换作品实体为 DTO
        return artworks.stream()
                .map(artwork -> {
                    ArtworkDTO dto = new ArtworkDTO();
                    dto.setId(artwork.getId());
                    dto.setArtistId(artwork.getArtistId());
                    dto.setArtistName(artwork.getArtistName());
                    // 优先使用冗余字段，不存在时从用户服务补全
                    String avatar = artwork.getArtistAvatar();
                    if (avatar == null || avatar.isEmpty()) {
                        avatar = avatarMap.get(artwork.getArtistId());
                    }
                    dto.setArtistAvatar(avatar);
                    dto.setTitle(artwork.getTitle());
                    dto.setDescription(artwork.getDescription());
                    dto.setImageUrl(artwork.getImageUrl());
                    dto.setThumbnailUrl(artwork.getThumbnailUrl());
                    dto.setViewCount(artwork.getViewCount());
                    dto.setLikeCount(artwork.getLikeCount());
                    dto.setFavoriteCount(artwork.getFavoriteCount());
                    dto.setCommentCount(artwork.getCommentCount());
                    dto.setHotnessScore(artwork.getHotnessScore());
                    dto.setStatus(artwork.getStatus());
                    dto.setIsAigc(artwork.getIsAigc());
                    dto.setCreatedAt(artwork.getCreatedAt());
                    dto.setUpdatedAt(artwork.getUpdatedAt());

                    // 设置图片（从映射中获取）
                    dto.setImageCount(artwork.getImageCount() != null ? artwork.getImageCount() : 1);
                    dto.setImages(artworkImagesMap.getOrDefault(artwork.getId(), new ArrayList<>()));

                    // 设置标签（从映射中获取）
                    List<TagDTO> tags = artworkTagsMap.getOrDefault(artwork.getId(), new ArrayList<>());
                    dto.setTags(tags);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将作品实体转换为 DTO
     * 
     * @param artwork 作品实体
     * @return 作品 DTO
     */
    private ArtworkDTO convertToDTO(Artwork artwork) {
        ArtworkDTO dto = new ArtworkDTO();
        dto.setId(artwork.getId());
        dto.setArtistId(artwork.getArtistId());
        dto.setArtistName(artwork.getArtistName());
        // 头像回填：旧数据可能没有冗余字段
        String avatar = artwork.getArtistAvatar();
        if (avatar == null || avatar.isEmpty()) {
            try {
                Result<UserDTO> ur = userServiceClient.getUserById(artwork.getArtistId());
                if (ur != null && ur.isSuccess() && ur.getData() != null) {
                    avatar = ur.getData().getAvatarUrl();
                }
            } catch (Exception e) {
                logger.warn("获取用户头像失败: userId={}", artwork.getArtistId());
            }
        }
        dto.setArtistAvatar(avatar);
        dto.setTitle(artwork.getTitle());
        dto.setDescription(artwork.getDescription());
        dto.setImageUrl(artwork.getImageUrl());
        dto.setThumbnailUrl(artwork.getThumbnailUrl());
        dto.setViewCount(artwork.getViewCount());
        dto.setLikeCount(artwork.getLikeCount());
        dto.setFavoriteCount(artwork.getFavoriteCount());
        dto.setCommentCount(artwork.getCommentCount());
        dto.setHotnessScore(artwork.getHotnessScore());
        dto.setStatus(artwork.getStatus());
        dto.setIsAigc(artwork.getIsAigc());
        dto.setCreatedAt(artwork.getCreatedAt());
        dto.setUpdatedAt(artwork.getUpdatedAt());

        // 加载图片
        dto.setImageCount(artwork.getImageCount() != null ? artwork.getImageCount() : 1);
        List<ArtworkImage> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artwork.getId());
        dto.setImages(images.stream()
                .map(img -> new ImageDTO(img.getImageUrl(), img.getOriginalImageUrl(), img.getThumbnailUrl(),
                        img.getSortOrder()))
                .collect(Collectors.toList()));

        // 加载标签
        List<ArtworkTag> artworkTags = artworkTagRepository.findByArtworkId(artwork.getId());
        List<TagDTO> tagDTOs = artworkTags.stream()
                .map(this::convertTagToDTO)
                .collect(Collectors.toList());
        dto.setTags(tagDTOs);

        return dto;
    }

    /**
     * 将作品实体转换为详情 DTO
     * 
     * @param artwork 作品实体
     * @return 作品详情 DTO
     */
    private ArtworkDetailDTO convertToDetailDTO(Artwork artwork) {
        ArtworkDetailDTO dto = new ArtworkDetailDTO();
        dto.setId(artwork.getId());
        dto.setArtistId(artwork.getArtistId());
        dto.setArtistName(artwork.getArtistName());
        // 头像回填：旧数据可能没有冗余字段
        String detailAvatar = artwork.getArtistAvatar();
        if (detailAvatar == null || detailAvatar.isEmpty()) {
            try {
                Result<UserDTO> ur = userServiceClient.getUserById(artwork.getArtistId());
                if (ur != null && ur.isSuccess() && ur.getData() != null) {
                    detailAvatar = ur.getData().getAvatarUrl();
                }
            } catch (Exception e) {
                logger.warn("获取用户头像失败: userId={}", artwork.getArtistId());
            }
        }
        dto.setArtistAvatar(detailAvatar);
        dto.setTitle(artwork.getTitle());
        dto.setDescription(artwork.getDescription());
        dto.setImageUrl(artwork.getImageUrl());
        dto.setThumbnailUrl(artwork.getThumbnailUrl());
        dto.setViewCount(artwork.getViewCount());
        dto.setLikeCount(artwork.getLikeCount());
        dto.setFavoriteCount(artwork.getFavoriteCount());
        dto.setCommentCount(artwork.getCommentCount());
        dto.setHotnessScore(artwork.getHotnessScore());
        dto.setStatus(artwork.getStatus());
        dto.setIsAigc(artwork.getIsAigc());
        dto.setCreatedAt(artwork.getCreatedAt());
        dto.setUpdatedAt(artwork.getUpdatedAt());

        // 加载图片
        dto.setImageCount(artwork.getImageCount() != null ? artwork.getImageCount() : 1);
        List<ArtworkImage> detailImages = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artwork.getId());
        dto.setImages(detailImages.stream()
                .map(img -> new ImageDTO(img.getImageUrl(), img.getOriginalImageUrl(), img.getThumbnailUrl(),
                        img.getSortOrder()))
                .collect(Collectors.toList()));

        // 加载标签
        List<ArtworkTag> artworkTags = artworkTagRepository.findByArtworkId(artwork.getId());
        List<TagDTO> tagDTOs = artworkTags.stream()
                .map(this::convertTagToDTO)
                .collect(Collectors.toList());
        dto.setTags(tagDTOs);

        return dto;
    }

    /**
     * 将作品标签关联转换为标签 DTO
     * 
     * @param artworkTag 作品标签关联
     * @return 标签 DTO
     */
    private TagDTO convertTagToDTO(ArtworkTag artworkTag) {
        Tag tag = tagRepository.findById(artworkTag.getTagId())
                .orElse(null);

        if (tag == null) {
            return null;
        }

        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setSource(artworkTag.getSource());
        dto.setConfidence(artworkTag.getConfidence());

        return dto;
    }

    /**
     * 获取用户收藏的作品列表
     *
     * @param userId 用户 ID
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 分页作品列表
     */
    public PageResult<ArtworkDTO> getUserFavoriteArtworks(Long userId, int page, int size) {
        logger.info("查询用户收藏作品列表: userId={}, page={}, size={}", userId, page, size);

        // 创建分页对象（Spring Data 的页码从 0 开始）
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 查询用户收藏的作品 ID 列表（分页）
        Page<Long> artworkIdPage = favoriteRepository.findArtworkIdsByUserIdPaged(userId, pageable);

        if (artworkIdPage.isEmpty()) {
            logger.info("用户没有收藏作品: userId={}", userId);
            return new PageResult<>(new ArrayList<>(), 0L, page, size);
        }

        // 根据作品 ID 列表查询作品详情
        List<Artwork> artworks = artworkRepository.findAllById(artworkIdPage.getContent());

        // 批量转换为 DTO
        List<ArtworkDTO> artworkDTOs = convertToDTOBatch(artworks);

        logger.info("用户收藏作品列表查询成功: userId={}, 总数={}, 当前页={}",
                userId, artworkIdPage.getTotalElements(), page);

        return new PageResult<>(
                artworkDTOs,
                artworkIdPage.getTotalElements(),
                page,
                size);
    }

    /**
     * 获取画师的作品数量
     *
     * @param artistId 画师 ID
     * @return 作品数量
     */
    @Cacheable(value = "artworkCount", key = "#p0")
    public long getArtworkCountByArtist(Long artistId) {
        logger.info("获取画师作品数量: artistId={}", artistId);
        return artworkRepository.countByArtistIdAndStatus(artistId, ArtworkStatus.PUBLISHED);
    }

    /**
     * 删除作品
     * 
     * @param artworkId 作品 ID
     * @param artistId  画师 ID（用于权限验证）
     */
    @CacheEvict(value = "artworkCount", key = "#p1")
    @Transactional
    public void deleteArtwork(Long artworkId, Long artistId) {
        logger.info("删除作品: artworkId={}, artistId={}", artworkId, artistId);

        // 1. 查找作品
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: id=" + artworkId));

        // 2. 验证权限：只有作品的创建者才能删除
        if (!artwork.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除此作品");
        }

        // 3. 收集需要删除的 OSS 文件 URL
        List<String> fileUrlsToDelete = new ArrayList<>();
        List<ArtworkImage> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId);
        for (ArtworkImage img : images) {
            if (img.getImageUrl() != null && !img.getImageUrl().isBlank()) {
                fileUrlsToDelete.add(img.getImageUrl());
            }
            if (img.getThumbnailUrl() != null && !img.getThumbnailUrl().isBlank()) {
                fileUrlsToDelete.add(img.getThumbnailUrl());
            }
        }
        // 也收集作品实体自身的封面图 URL（可能与 images 重复，但删除操作幂等）
        if (artwork.getImageUrl() != null && !artwork.getImageUrl().isBlank()) {
            fileUrlsToDelete.add(artwork.getImageUrl());
        }
        if (artwork.getThumbnailUrl() != null && !artwork.getThumbnailUrl().isBlank()) {
            fileUrlsToDelete.add(artwork.getThumbnailUrl());
        }

        // 4. 删除关联图片记录
        artworkImageRepository.deleteByArtworkId(artworkId);

        // 5. 删除作品（物理删除）
        artworkRepository.delete(artwork);

        // 6. 清除缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 7. 从排行榜移除
        rankingService.removeFromRanking(artworkId);

        // 8. 异步删除 OSS 文件（不影响主流程）
        if (!fileUrlsToDelete.isEmpty()) {
            try {
                logger.info("开始删除 OSS 文件: artworkId={}, 文件数={}", artworkId, fileUrlsToDelete.size());
                Result<Integer> deleteResult = fileServiceClient.deleteFiles(fileUrlsToDelete);
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    logger.info("OSS 文件删除成功: artworkId={}, 删除数={}", artworkId, deleteResult.getData());
                } else {
                    logger.warn("OSS 文件删除部分失败: artworkId={}, result={}", artworkId, deleteResult);
                }
            } catch (Exception e) {
                // OSS 删除失败不影响作品删除的主流程
                logger.error("删除 OSS 文件异常（不影响作品删除）: artworkId={}", artworkId, e);
            }
        }

        logger.info("作品删除成功: artworkId={}", artworkId);
    }

    /**
     * 获取用户的草稿列表
     */
    public PageResult<ArtworkDTO> getDrafts(Long artistId, int page, int size) {
        logger.info("获取草稿列表: artistId={}, page={}, size={}", artistId, page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Artwork> artworkPage = artworkRepository.findByArtistIdAndStatus(artistId, ArtworkStatus.DRAFT, pageable);
        List<ArtworkDTO> dtos = convertToDTOBatch(artworkPage.getContent());
        return new PageResult<>(dtos, artworkPage.getTotalElements(), page, size);
    }

    /**
     * 发布草稿
     */
    @Transactional
    public ArtworkDTO publishDraft(Long artworkId, Long artistId) {
        logger.info("发布草稿: artworkId={}, artistId={}", artworkId, artistId);

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("草稿不存在: id=" + artworkId));

        if (!artwork.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此草稿");
        }

        if (artwork.getStatus() != ArtworkStatus.DRAFT) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该作品不是草稿状态");
        }

        if (artwork.getImageUrl() == null || artwork.getImageUrl().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请至少上传一张图片后再发布");
        }

        artwork.setStatus(ArtworkStatus.PUBLISHED);
        artworkRepository.save(artwork);

        // 发送智能打标
        taggingQueueService.sendTaggingRequest(artwork.getId(), artwork.getImageUrl());

        // 推送到粉丝 Feed 收件箱
        pushToFollowerFeeds(artistId, artwork.getId());

        logger.info("草稿发布成功: artworkId={}", artworkId);
        return convertToDTO(artwork);
    }

    /**
     * 搜索建议：返回匹配的标签名 + 作品标题
     */
    @Cacheable(value = "searchSuggestions", key = "#p0 + '_' + #p1")
    public List<SearchSuggestionDTO> getSearchSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = keyword.trim();
        int half = Math.max(limit / 2, 3);
        Pageable pageable = PageRequest.of(0, half);

        List<SearchSuggestionDTO> suggestions = new ArrayList<>();

        // 1. 标签建议
        try {
            List<Tag> matchingTags = tagRepository.searchByKeyword(trimmed, pageable);
            for (Tag tag : matchingTags) {
                suggestions.add(new SearchSuggestionDTO("tag", tag.getName(), tag.getUsageCount()));
            }
        } catch (Exception e) {
            logger.warn("搜索标签建议失败: {}", e.getMessage());
        }

        // 2. 标题建议
        try {
            List<String> titles = artworkRepository.findTitleSuggestions(trimmed, ArtworkStatus.PUBLISHED, pageable);
            for (String title : titles) {
                suggestions.add(new SearchSuggestionDTO("artwork", title, null));
            }
        } catch (Exception e) {
            logger.warn("搜索标题建议失败: {}", e.getMessage());
        }

        // 限制总数
        if (suggestions.size() > limit) {
            suggestions = suggestions.subList(0, limit);
        }
        return suggestions;
    }

    /**
     * 搜索建议 DTO（内部类）
     */
    /**
     * 将作品推送到所有粉丝的 Feed 收件箱（异步容错）
     *
     * @param artistId  画师 ID
     * @param artworkId 作品 ID
     */
    private void pushToFollowerFeeds(Long artistId, Long artworkId) {
        try {
            Result<List<Long>> followerResult = userServiceClient.getFollowerIds(artistId);
            if (followerResult != null && followerResult.getCode() == 200 && followerResult.getData() != null) {
                feedService.pushToFollowerFeeds(followerResult.getData(), artworkId);
            }
        } catch (Exception e) {
            logger.warn("推送 Feed 失败（不影响发布）: artistId={}, artworkId={}, error={}",
                    artistId, artworkId, e.getMessage());
        }
    }

    /**
     * 搜索建议 DTO（内部类）
     */
    public static class SearchSuggestionDTO {
        private String type;
        private String text;
        private Integer count;

        public SearchSuggestionDTO(String type, String text, Integer count) {
            this.type = type;
            this.text = text;
            this.count = count;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

}
