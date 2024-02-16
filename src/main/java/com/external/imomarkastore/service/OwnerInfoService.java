package com.external.imomarkastore.service;

import com.external.imomarkastore.constant.OwnerState;

public interface OwnerInfoService {

    void updateState(OwnerState state);

    boolean isOwner(Long telegramUserId);

    OwnerState getCurrentOwnerState();
}
