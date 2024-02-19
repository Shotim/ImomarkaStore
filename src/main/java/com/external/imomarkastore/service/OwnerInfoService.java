package com.external.imomarkastore.service;

import com.external.imomarkastore.constant.OwnerState;
import com.google.gson.JsonObject;

public interface OwnerInfoService {

    boolean isOwner(Long telegramUserId);

    void updateState(OwnerState state);

    void updateName(String name);
    void updatePhoneNumber(String phoneNumber);

    OwnerState getCurrentOwnerState();

    void updateJsonData(String jsonData);

    String getJsonData();

    JsonObject getJsonDataObject();

    String createContactsPayload();
}
