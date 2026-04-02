package com.pixiv.artwork.search;

import com.pixiv.artwork.document.ArtworkDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch 作品搜索仓库
 * 
 * 提供基于 Elasticsearch 的作品索引 CRUD 操作
 * 放在独立的 search 包中，避免与 JPA Repository 扫描冲突
 */
@Repository
public interface ArtworkSearchRepository extends ElasticsearchRepository<ArtworkDocument, Long> {
}
