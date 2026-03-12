package com.pixiv.user.repository;

import com.pixiv.user.entity.ArtistWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistWalletRepository extends JpaRepository<ArtistWallet, Long> {

    Optional<ArtistWallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
