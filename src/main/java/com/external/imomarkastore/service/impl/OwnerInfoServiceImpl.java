package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.model.OwnerInfo;
import com.external.imomarkastore.repository.OwnerInfoRepository;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class OwnerInfoServiceImpl implements OwnerInfoService {

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
                            throw new InternalError();
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
                            throw new InternalError();
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
                            throw new InternalError();
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
                .orElseThrow(InternalError::new);
    }

    @Override
    public void updateJsonData(String jsonData) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setJsonData(jsonData);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new InternalError();
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
                .orElseThrow(InternalError::new);
    }

    private String getPayload(OwnerInfo ownerInfo) {
        return messageSource.getMessage("shopInfo", List.of(
                isBlank(ownerInfo.getOwnerName()) ? EMPTY : ownerInfo.getOwnerName(),
                isBlank(ownerInfo.getPhoneNumber()) ? EMPTY : ownerInfo.getPhoneNumber(),
                isBlank(ownerInfo.getAddress()) ? EMPTY : ownerInfo.getAddress(),
                isBlank(ownerInfo.getEmail()) ? EMPTY : ownerInfo.getEmail(),
                isBlank(ownerInfo.getInn()) ? EMPTY : ownerInfo.getInn()
        ).toArray());
    }

    private Optional<OwnerInfo> getOwnerInfoOptional() {
        return repository.findAll().stream()
                .findFirst();
    }
}
