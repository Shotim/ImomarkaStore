package com.external.imomarkastore.telegramapi.message.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.util.BotMessageSource;
import org.springframework.stereotype.Service;

import static com.external.imomarkastore.constant.ClientState.BACK_TO_MAIN_MENU;

@Service
public class BackToMainMenuExecutionService extends AbstractMainMenuExecutionService {

    public BackToMainMenuExecutionService(ClientInfoService clientInfoService, InomarkaStore inomarkaStore, BotMessageSource messageSource) {
        super(clientInfoService, inomarkaStore, messageSource);
    }

    @Override
    public ClientState getState() {
        return BACK_TO_MAIN_MENU;
    }
}
