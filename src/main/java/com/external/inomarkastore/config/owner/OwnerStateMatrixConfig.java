package com.external.inomarkastore.config.owner;

import com.external.inomarkastore.constant.OwnerState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static com.external.inomarkastore.constant.OwnerState.BACK_FROM_BLACK_LIST;
import static com.external.inomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.inomarkastore.constant.OwnerState.CONFIRM_PAYMENT;
import static com.external.inomarkastore.constant.OwnerState.DELETE_APPLICATION;
import static com.external.inomarkastore.constant.OwnerState.EDIT_ADDRESS;
import static com.external.inomarkastore.constant.OwnerState.EDIT_EMAIL;
import static com.external.inomarkastore.constant.OwnerState.EDIT_INN;
import static com.external.inomarkastore.constant.OwnerState.EDIT_NAME;
import static com.external.inomarkastore.constant.OwnerState.EDIT_PHONE_NUMBER;
import static com.external.inomarkastore.constant.OwnerState.EDIT_WORKING_HOURS;
import static com.external.inomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.inomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.inomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.inomarkastore.constant.OwnerState.GET_BLACK_LIST;
import static com.external.inomarkastore.constant.OwnerState.GET_CLIENTS;
import static com.external.inomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.inomarkastore.constant.OwnerState.GET_PHOTO;
import static com.external.inomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.inomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.inomarkastore.constant.OwnerState.MOVE_CLIENT_TO_BLACK_LIST;
import static com.external.inomarkastore.constant.OwnerState.PREPARE_DISTRIBUTION;
import static com.external.inomarkastore.constant.OwnerState.PREPARE_GET_PHOTO;
import static com.external.inomarkastore.constant.OwnerState.PREPARE_PAYMENT;
import static com.external.inomarkastore.constant.OwnerState.RESTORE_APPLICATION;
import static com.external.inomarkastore.constant.OwnerState.SAVE_ADDRESS;
import static com.external.inomarkastore.constant.OwnerState.SAVE_EMAIL;
import static com.external.inomarkastore.constant.OwnerState.SAVE_INN;
import static com.external.inomarkastore.constant.OwnerState.SAVE_NAME;
import static com.external.inomarkastore.constant.OwnerState.SAVE_PHONE_NUMBER;
import static com.external.inomarkastore.constant.OwnerState.SAVE_WORKING_HOURS;
import static com.external.inomarkastore.constant.OwnerState.SEND_DISTRIBUTION;
import static com.external.inomarkastore.constant.OwnerState.SEND_PAYMENT;
import static com.external.inomarkastore.constant.OwnerState.SET_PAYMENT;
import static com.external.inomarkastore.constant.OwnerState.START;
import static java.util.Map.entry;

@Configuration
public class OwnerStateMatrixConfig {

    @Bean("ownerStateMatrix")
    public Map<OwnerState, List<OwnerState>> createOwnerStateMatrix() {
        return Map.ofEntries(
                entry(START,
                        List.of(MAIN_MENU)),
                entry(MAIN_MENU,
                        List.of(GET_APPLICATIONS, GET_ARCHIVED_APPLICATIONS, GET_CLIENTS,
                                GET_BLACK_LIST, GET_CONTACTS, PREPARE_PAYMENT, PREPARE_GET_PHOTO, PREPARE_DISTRIBUTION)),
                entry(GET_APPLICATIONS,
                        List.of(EXPORT_APPLICATIONS, MOVE_APPLICATION_TO_ARCHIVE, BACK_TO_MAIN_MENU, PREPARE_PAYMENT, SET_PAYMENT)),
                entry(GET_ARCHIVED_APPLICATIONS,
                        List.of(EXPORT_APPLICATIONS, DELETE_APPLICATION, RESTORE_APPLICATION, BACK_TO_MAIN_MENU)),
                entry(GET_CLIENTS,
                        List.of(MOVE_CLIENT_TO_BLACK_LIST, BACK_TO_MAIN_MENU)),
                entry(GET_BLACK_LIST,
                        List.of(BACK_FROM_BLACK_LIST, BACK_TO_MAIN_MENU)),
                entry(GET_CONTACTS,
                        List.of(EDIT_NAME, EDIT_PHONE_NUMBER, EDIT_ADDRESS, EDIT_INN, EDIT_EMAIL, EDIT_WORKING_HOURS, BACK_TO_MAIN_MENU)),
                entry(EDIT_NAME,
                        List.of(SAVE_NAME)),
                entry(EDIT_PHONE_NUMBER,
                        List.of(SAVE_PHONE_NUMBER)),
                entry(EDIT_ADDRESS,
                        List.of(SAVE_ADDRESS)),
                entry(EDIT_INN,
                        List.of(SAVE_INN)),
                entry(EDIT_EMAIL,
                        List.of(SAVE_EMAIL)),
                entry(EDIT_WORKING_HOURS,
                        List.of(SAVE_WORKING_HOURS)),
                entry(SAVE_NAME, List.of(GET_CONTACTS)),
                entry(SAVE_PHONE_NUMBER, List.of(GET_CONTACTS)),
                entry(SAVE_ADDRESS, List.of(GET_CONTACTS)),
                entry(SAVE_INN, List.of(GET_CONTACTS)),
                entry(SAVE_EMAIL, List.of(GET_CONTACTS)),
                entry(SAVE_WORKING_HOURS, List.of(GET_CONTACTS)),
                entry(PREPARE_PAYMENT, List.of(SEND_PAYMENT, GET_APPLICATIONS)),
                entry(SEND_PAYMENT, List.of(GET_APPLICATIONS, BACK_TO_MAIN_MENU)),
                entry(PREPARE_GET_PHOTO, List.of(GET_PHOTO, BACK_TO_MAIN_MENU)),
                entry(SET_PAYMENT, List.of(CONFIRM_PAYMENT, GET_APPLICATIONS)),
                entry(CONFIRM_PAYMENT, List.of(GET_APPLICATIONS, BACK_TO_MAIN_MENU)),
                entry(PREPARE_DISTRIBUTION, List.of(SEND_DISTRIBUTION, BACK_TO_MAIN_MENU)),
                entry(SEND_DISTRIBUTION, List.of(SEND_DISTRIBUTION, BACK_TO_MAIN_MENU))
        );
    }
}