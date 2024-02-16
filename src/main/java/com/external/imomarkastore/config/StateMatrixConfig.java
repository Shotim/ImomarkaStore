package com.external.imomarkastore.config;

import com.external.imomarkastore.constant.ClientState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static com.external.imomarkastore.constant.ClientState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.imomarkastore.constant.ClientState.CREATE_APPLICATION;
import static com.external.imomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.imomarkastore.constant.ClientState.EDIT_NAME;
import static com.external.imomarkastore.constant.ClientState.EDIT_PHONE_NUMBER;
import static com.external.imomarkastore.constant.ClientState.GET_CARS;
import static com.external.imomarkastore.constant.ClientState.INITIAL_SET_NAME;
import static com.external.imomarkastore.constant.ClientState.INITIAL_SET_PHONE_NUMBER;
import static com.external.imomarkastore.constant.ClientState.INITIAL_START;
import static com.external.imomarkastore.constant.ClientState.INSERT_CAR_DETAILS;
import static com.external.imomarkastore.constant.ClientState.INSERT_COMMENT;
import static com.external.imomarkastore.constant.ClientState.INSERT_MAIN_PURPOSE;
import static com.external.imomarkastore.constant.ClientState.INSERT_VIN_NUMBER;
import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.constant.ClientState.REPEATED_START;
import static com.external.imomarkastore.constant.ClientState.SAVE_NAME;
import static com.external.imomarkastore.constant.ClientState.SAVE_PHONE_NUMBER;
import static java.util.Map.entry;

@Configuration
public class StateMatrixConfig {

    @Bean("stateMatrix")
    public Map<ClientState, List<ClientState>> createStateMatrix() {
        return Map.ofEntries(entry(INITIAL_START, List.of(INITIAL_SET_NAME)),
                entry(INITIAL_SET_NAME, List.of(INITIAL_SET_PHONE_NUMBER)),
                entry(INITIAL_SET_PHONE_NUMBER, List.of(MAIN_MENU)),
                entry(REPEATED_START, List.of(MAIN_MENU)),
                entry(MAIN_MENU, List.of(CREATE_APPLICATION, EDIT_NAME, EDIT_PHONE_NUMBER, GET_CARS)),
                entry(CREATE_APPLICATION, List.of(INSERT_CAR_DETAILS, CHOOSE_CAR_FOR_APPLICATION)),
                entry(INSERT_CAR_DETAILS, List.of(INSERT_VIN_NUMBER)),
                entry(CHOOSE_CAR_FOR_APPLICATION, List.of(INSERT_MAIN_PURPOSE)),
                entry(INSERT_VIN_NUMBER, List.of(INSERT_MAIN_PURPOSE)),
                entry(INSERT_MAIN_PURPOSE, List.of(INSERT_COMMENT)),
                entry(INSERT_COMMENT, List.of(MAIN_MENU)),
                entry(EDIT_NAME, List.of(SAVE_NAME)),
                entry(EDIT_PHONE_NUMBER, List.of(SAVE_PHONE_NUMBER)),
                entry(SAVE_NAME, List.of(MAIN_MENU)),
                entry(SAVE_PHONE_NUMBER, List.of(MAIN_MENU)),
                entry(GET_CARS, List.of(BACK_TO_MAIN_MENU, DELETE_CAR)));
    }

    @Bean("callbackExecutionClientStates")
    public List<ClientState> callbackExecutionStates() {
        return List.of(CHOOSE_CAR_FOR_APPLICATION, DELETE_CAR, INSERT_COMMENT);
    }

    @Bean("messageExecutionClientStates")
    public List<ClientState> messageExecutionStates() {
        return List.of(
                BACK_TO_MAIN_MENU,
                CREATE_APPLICATION,
                EDIT_NAME,
                EDIT_PHONE_NUMBER,
                GET_CARS,
                INITIAL_SET_NAME,
                INITIAL_SET_PHONE_NUMBER,
                INSERT_CAR_DETAILS,
                INSERT_COMMENT,
                INSERT_MAIN_PURPOSE,
                INSERT_VIN_NUMBER,
                SAVE_NAME,
                SAVE_PHONE_NUMBER);
    }
}
