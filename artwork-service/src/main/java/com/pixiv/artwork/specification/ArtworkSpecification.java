package com.pixiv.artwork.specification;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作品动态查询规约
 * 使用 JPA Specification 实现多条件组合的高级搜索
 */
public class ArtworkSpecification {

    private ArtworkSpecification() {
    }

    /**
     * 构建高级搜索规约
     *
     * @param keyword    关键词（模糊匹配标题、描述、画师名）
     * @param isAigc     AI 生成筛选（null=不筛选, true=仅AI, false=仅人工）
     * @param dateFrom   开始日期
     * @param dateTo     结束日期
     * @param minLikes   最低点赞数
     * @param artworkIds 标签过滤后的作品ID列表（null=不按标签筛选）
     * @param artistId   画师ID（null=不按画师筛选）
     * @return Specification
     */
    public static Specification<Artwork> advancedSearch(
            String keyword,
            Boolean isAigc,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Integer minLikes,
            List<Long> artworkIds,
            Long artistId) {

        return (Root<Artwork> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 必须是已发布状态
            predicates.add(cb.equal(root.get("status"), ArtworkStatus.PUBLISHED));

            // 2. 关键词搜索（匹配标题、描述、画师名）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), kw),
                        cb.like(cb.lower(root.get("description")), kw),
                        cb.like(cb.lower(root.get("artistName")), kw)));
            }

            // 3. AIGC 筛选
            if (isAigc != null) {
                predicates.add(cb.equal(root.get("isAigc"), isAigc));
            }

            // 4. 时间范围筛选
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            // 5. 最低点赞数
            if (minLikes != null && minLikes > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("likeCount"), minLikes));
            }

            // 6. 标签过滤（通过预查ID列表实现）
            if (artworkIds != null && !artworkIds.isEmpty()) {
                predicates.add(root.get("id").in(artworkIds));
            }

            // 7. 画师ID筛选
            if (artistId != null) {
                predicates.add(cb.equal(root.get("artistId"), artistId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
