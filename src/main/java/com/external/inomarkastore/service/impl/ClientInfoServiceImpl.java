package com.external.inomarkastore.service.impl;

import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.repository.ClientInfoRepository;
import com.external.inomarkastore.service.ClientInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.external.inomarkastore.constant.ClientState.INITIAL_START;
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
                        new BusinessLogicException("Could not find client by telegram user id: %s".formatted(telegramUserId)));
    }

    @Override
    public ClientInfo getByTelegramUserIdOrPhoneNumber(Long telegramUserId, String phoneNumber) {
        return repository.findByTelegramUserIdOrPhoneNumber(telegramUserId, phoneNumber)
                .orElseThrow(() ->
                        new BusinessLogicException("Could not find client by telegram user id: %s or phoneNumber: %s".formatted(telegramUserId, phoneNumber)));
    }

    @Override
    public Optional<ClientInfo> getByTelegramUserIdOpt(Long telegramUserId) {
        return repository.findByTelegramUserId(telegramUserId);
    }

    @Override
    public Optional<ClientInfo> getByPhoneNumberOpt(String phoneNumber) {
        return repository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public ClientInfo create(Long telegramUserId, String telegramUserName) {
        final var clientInfo = new ClientInfo();
        clientInfo.setTelegramUserId(telegramUserId);
        clientInfo.setTelegramUserName("@" + telegramUserName);
        clientInfo.setId(randomUUID());
        clientInfo.setState(INITIAL_START);
        clientInfo.setIsInBlackList(false);
        return repository.save(clientInfo);
    }

    @Override
    public ClientInfo create(String name, String phoneNumber) {
        final var clientInfo = new ClientInfo();
        clientInfo.setId(randomUUID());
        clientInfo.setName(name);
        clientInfo.setPhoneNumber(phoneNumber);
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

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
