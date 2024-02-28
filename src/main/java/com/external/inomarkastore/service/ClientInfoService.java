package com.external.inomarkastore.service;

import com.external.inomarkastore.model.ClientInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientInfoService {
    ClientInfo getById(UUID id);

    ClientInfo getByTelegramUserId(Long telegramUserId);

    ClientInfo getByTelegramUserIdOrPhoneNumber(Long telegramUserId, String phoneNumber);

    Optional<ClientInfo> getByTelegramUserIdOpt(Long telegramUserId);

    Optional<ClientInfo> getByPhoneNumberOpt(String phoneNumber);

    ClientInfo create(Long telegramUserId, String telegramUserName);

    ClientInfo create(String name, String phoneNumber);

    ClientInfo update(ClientInfo clientInfo);

    List<ClientInfo> getActiveClients();

    List<ClientInfo> getBlackListClients();

    List<Long> getTelegramUserIds();

    void deleteById(UUID id);
}
