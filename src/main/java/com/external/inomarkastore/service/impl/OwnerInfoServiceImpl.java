package com.external.inomarkastore.service.impl;

import com.external.inomarkastore.constant.OwnerState;
import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.OwnerInfo;
import com.external.inomarkastore.repository.OwnerInfoRepository;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class OwnerInfoServiceImpl implements OwnerInfoService {

    private static final String COULD_NOT_FIND_OWNER_ENTITY_IN_DB = "Could not find owner entity in db";
    private final OwnerInfoRepository repository;
    private final BotMessageSource messageSource;

    @Override
    public void updateState(OwnerState state) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setOwnerState(state);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updateName(String name) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setOwnerName(name);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updatePhoneNumber(String phoneNumber) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setPhoneNumber(phoneNumber);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updateAddress(String address) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setAddress(address);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updateEmail(String email) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setEmail(email);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updateInn(String inn) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setInn(inn);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public void updateWorkingHours(String workingHours) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setWorkingHours(workingHours);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public boolean isOwner(Long telegramUserId) {
        return repository.findById(telegramUserId).isPresent();
    }

    @Override
    public OwnerState getCurrentOwnerState() {
        return getOwnerInfoOptional()
                .map(OwnerInfo::getOwnerState)
                .orElseThrow(() -> new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB));
    }

    @Override
    public Long getTelegramUserId() {
        return getOwnerInfoOptional()
                .map(OwnerInfo::getTelegramUserId)
                .orElseThrow(() -> new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB));
    }

    @Override
    public void updateJsonData(String jsonData) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setJsonData(jsonData);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB);
                        });
    }

    @Override
    public String getJsonData() {
        return getOwnerInfoOptional()
                .map(OwnerInfo::getJsonData)
                .orElse(EMPTY);
    }

    @Override
    public JsonObject getJsonDataObject() {
        final var jsonData = this.getJsonData();
        return isBlank(jsonData) ?
                new JsonObject() :
                new Gson().fromJson(jsonData, JsonObject.class);
    }

    @Override
    public String createContactsPayload() {
        return getOwnerInfoOptional()
                .map(this::getPayload)
                .orElseThrow(() -> new BusinessLogicException(COULD_NOT_FIND_OWNER_ENTITY_IN_DB));
    }

    private String getPayload(OwnerInfo ownerInfo) {
        return messageSource.getMessage("shopInfo", Stream.of(
                        ownerInfo.getOwnerName(),
                        ownerInfo.getPhoneNumber(),
                        ownerInfo.getAddress(),
                        ownerInfo.getEmail(),
                        ownerInfo.getInn(),
                        ownerInfo.getWorkingHours())
                .map(string -> isBlank(string) ? EMPTY : string)
                .toArray());
    }

    private Optional<OwnerInfo> getOwnerInfoOptional() {
        return repository.findAll().stream()
                .findFirst();
    }
}
