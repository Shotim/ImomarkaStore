package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.exception.BusinessLogicException;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.repository.ClientInfoRepository;
import com.external.imomarkastore.service.ClientInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.external.imomarkastore.constant.ClientState.INITIAL_START;
import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class ClientInfoServiceImpl implements ClientInfoService {

    private final ClientInfoRepository repository;

    @Override
    public ClientInfo getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessLogicException("Could not find client by id: %s".formatted(id)));
    }

    @Override
    public ClientInfo getByTelegramUserId(Long telegramUserId) {
        return repository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() ->
                        new BusinessLogicException("Could not find client by id: %s".formatted(telegramUserId)));
    }

    @Override
    public ClientInfo create(Long telegramUserId) {
        final var clientInfo = new ClientInfo();
        clientInfo.setTelegramUserId(telegramUserId);
        clientInfo.setId(randomUUID());
        clientInfo.setState(INITIAL_START);
        clientInfo.setIsInBlackList(false);
        return repository.save(clientInfo);
    }

    @Override
    public ClientInfo update(ClientInfo clientInfo) {
        return repository.save(clientInfo);
    }

    @Override
    public List<ClientInfo> getActiveClients() {
        return repository.findActiveClients();
    }

    @Override
    public List<ClientInfo> getBlackListClients() {
        return repository.findBlackListedClients();
    }

    @Override
    public List<Long> getTelegramUserIds() {
        return repository.findTelegramUserIds();
    }
}
