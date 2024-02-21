package com.external.imomarkastore.service;

import com.external.imomarkastore.model.ClientInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientInfoService {
    Optional<ClientInfo> getById(UUID id);

    Optional<ClientInfo> getByTelegramUserId(Long telegramUserId);

    ClientInfo create(Long telegramUserId);

    ClientInfo update(ClientInfo clientInfo);

    List<ClientInfo> getActiveClients();

    List<ClientInfo> getBlackListClients();
}
