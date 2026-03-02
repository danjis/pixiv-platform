package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.ArtworkImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkImageRepository extends JpaRepository<ArtworkImage, Long> {

    List<ArtworkImage> findByArtworkIdOrderBySortOrderAsc(Long artworkId);

    List<ArtworkImage> findByArtworkIdInOrderBySortOrderAsc(List<Long> artworkIds);

    void deleteByArtworkId(Long artworkId);

    long countByArtworkId(Long artworkId);
}
