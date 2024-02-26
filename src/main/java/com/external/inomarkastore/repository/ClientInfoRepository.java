package com.external.inomarkastore.repository;

import com.external.inomarkastore.model.ClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientInfoRepository extends JpaRepository<ClientInfo, UUID> {
    Optional<ClientInfo> findByTelegramUserId(Long telegramUserId);

    @Query("SELECT ci FROM ClientInfo as ci WHERE ci.isInBlackList = false or ci.isInBlackList is null")
    List<ClientInfo> findActiveClients();

    @Query("SELECT ci FROM ClientInfo as ci WHERE ci.isInBlackList = true")
    List<ClientInfo> findBlackListedClients();

    @Query("SELECT DISTINCT ci.telegramUserId FROM ClientInfo AS ci WHERE ci.telegramUserId IS NOT NULL")
    List<Long> findTelegramUserIds();
}
