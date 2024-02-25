package com.external.imomarkastore.service;

import com.external.imomarkastore.model.ClientInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientInfoService {
    ClientInfo getById(UUID id);

    ClientInfo getByTelegramUserId(Long telegramUserId);

    Optional<ClientInfo> getByTelegramUserIdOpt(Long telegramUserId);

    ClientInfo create(Long telegramUserId, String telegramUserName);

    ClientInfo update(ClientInfo clientInfo);

    List<ClientInfo> getActiveClients();

    List<ClientInfo> getBlackListClients();

    List<Long> getTelegramUserIds();
}
