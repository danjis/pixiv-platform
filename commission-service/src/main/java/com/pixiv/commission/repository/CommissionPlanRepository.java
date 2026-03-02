package com.pixiv.commission.repository;

import com.pixiv.commission.entity.CommissionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, Long> {

    List<CommissionPlan> findByArtistIdAndActiveTrueOrderBySortOrderAsc(Long artistId);

    List<CommissionPlan> findByArtistIdOrderBySortOrderAsc(Long artistId);

    long countByArtistId(Long artistId);
}
