package com.external.imomarkastore.service;

import com.external.imomarkastore.constant.OwnerState;

public interface OwnerInfoService {

    boolean isOwner(Long telegramUserId);

    void updateState(OwnerState state);

    OwnerState getCurrentOwnerState();

    void updateJsonData(String jsonData);

    String getJsonData();
}
