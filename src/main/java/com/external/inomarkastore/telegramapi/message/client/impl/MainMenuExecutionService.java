package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.util.BotMessageSource;
import org.springframework.stereotype.Service;

import static com.external.inomarkastore.constant.ClientState.MAIN_MENU;

@Service
public class MainMenuExecutionService extends AbstractMainMenuExecutionService {
    public MainMenuExecutionService(ClientInfoService clientInfoService, InomarkaStore inomarkaStore, BotMessageSource messageSource) {
        super(clientInfoService, inomarkaStore, messageSource);
    }

    @Override
    public ClientState getState() {
        return MAIN_MENU;
    }
}
