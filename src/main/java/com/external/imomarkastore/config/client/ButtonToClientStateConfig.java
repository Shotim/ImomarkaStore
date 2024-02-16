package com.external.imomarkastore.config.client;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.external.imomarkastore.constant.ClientState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.imomarkastore.constant.ClientState.CREATE_APPLICATION;
import static com.external.imomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.imomarkastore.constant.ClientState.EDIT_NAME;
import static com.external.imomarkastore.constant.ClientState.EDIT_PHONE_NUMBER;
import static com.external.imomarkastore.constant.ClientState.GET_CARS;
import static com.external.imomarkastore.constant.ClientState.INSERT_COMMENT;

@Configuration
@RequiredArgsConstructor
public class ButtonToClientStateConfig {

    private final BotMessageSource messageSource;

    @Bean("buttonToClientStateMatrix")
    public Map<String, ClientState> buttonToClientStateMatrix() {
        return Map.of(messageSource.getMessage("buttonName.newApplication"), CREATE_APPLICATION,
                messageSource.getMessage("buttonName.editName"), EDIT_NAME,
                messageSource.getMessage("buttonName.editPhoneNumber"), EDIT_PHONE_NUMBER,
                messageSource.getMessage("buttonName.getCars"), GET_CARS,
                messageSource.getMessage("buttonName.deleteCar"), DELETE_CAR,
                messageSource.getMessage("buttonName.backToMainMenu"), BACK_TO_MAIN_MENU,
                messageSource.getMessage("buttonName.chooseCarForApplication"), CHOOSE_CAR_FOR_APPLICATION,
                messageSource.getMessage("buttonName.skipInsertComment"), INSERT_COMMENT);
    }
}
