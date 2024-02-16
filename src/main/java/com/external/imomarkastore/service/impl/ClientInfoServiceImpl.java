package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.repository.ClientInfoRepository;
import com.external.imomarkastore.service.ClientInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.external.imomarkastore.constant.ClientState.INITIAL_START;
import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class ClientInfoServiceImpl implements ClientInfoService {

    private final ClientInfoRepository repository;
    @Override
    public Optional<ClientInfo> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ClientInfo> getByTelegramUserId(Long telegramUserId) {
        return repository.findByTelegramUserId(telegramUserId);
    }

    @Override
    public ClientInfo create(Long telegramUserId) {
        final var clientInfo = new ClientInfo();
        clientInfo.setTelegramUserId(telegramUserId);
        clientInfo.setId(randomUUID());
        clientInfo.setState(INITIAL_START);
        return repository.save(clientInfo);
    }

    @Override
    public ClientInfo update(ClientInfo clientInfo) {
        return repository.save(clientInfo);
    }
}
