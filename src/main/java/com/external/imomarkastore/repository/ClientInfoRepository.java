package com.external.imomarkastore.repository;

import com.external.imomarkastore.model.ClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientInfoRepository extends JpaRepository<ClientInfo, UUID> {

    Optional<ClientInfo> findByTelegramUserId(Long telegramUserId);
}
