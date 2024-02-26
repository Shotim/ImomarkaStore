package com.external.inomarkastore.service;

import com.external.inomarkastore.constant.OwnerState;
import com.google.gson.JsonObject;

public interface OwnerInfoService {
    boolean isOwner(Long telegramUserId);

    void updateState(OwnerState state);

    void updateName(String name);

    void updatePhoneNumber(String phoneNumber);

    void updateAddress(String address);

    void updateEmail(String email);

    void updateInn(String inn);

    void updateWorkingHours(String workingHours);

    OwnerState getCurrentOwnerState();

    void updateJsonData(String jsonData);

    String getJsonData();

    JsonObject getJsonDataObject();

    String createContactsPayload();

    Long getTelegramUserId();
}
