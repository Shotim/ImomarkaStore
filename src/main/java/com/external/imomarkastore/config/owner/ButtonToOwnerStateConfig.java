package com.external.imomarkastore.config.owner;

import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.EDIT_EMAIL;
import static com.external.imomarkastore.constant.OwnerState.EDIT_INN;
import static com.external.imomarkastore.constant.OwnerState.EDIT_NAME;
import static com.external.imomarkastore.constant.OwnerState.EDIT_PHONE_NUMBER;
import static com.external.imomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_BLACK_LIST;
import static com.external.imomarkastore.constant.OwnerState.GET_CLIENTS;
import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.PREPARE_GET_PHOTO;
import static java.util.Map.entry;

@Configuration
@RequiredArgsConstructor
public class ButtonToOwnerStateConfig {

    private final BotMessageSource messageSource;

    @Bean("buttonToOwnerStateMatrix")
    public Map<String, OwnerState> buttonToOwnerStateMatrix() {
        return Map.ofEntries(
                entry(messageSource.getMessage("buttonName.owner.getApplications"), GET_APPLICATIONS),
                entry(messageSource.getMessage("buttonName.owner.getArchivedApplications"), GET_ARCHIVED_APPLICATIONS),
                entry(messageSource.getMessage("buttonName.owner.getClients"), GET_CLIENTS),
                entry(messageSource.getMessage("buttonName.owner.getBlackList"), GET_BLACK_LIST),
                entry(messageSource.getMessage("buttonName.owner.getContacts"), GET_CONTACTS),
                entry(messageSource.getMessage("buttonName.owner.exportApplications"), EXPORT_APPLICATIONS),
                entry(messageSource.getMessage("buttonName.owner.backToMainMenu"), BACK_TO_MAIN_MENU),
                entry(messageSource.getMessage("buttonName.owner.editName"), EDIT_NAME),
                entry(messageSource.getMessage("buttonName.owner.editPhoneNumber"), EDIT_PHONE_NUMBER),
                entry(messageSource.getMessage("buttonName.owner.editAddress"), EDIT_EMAIL),
                entry(messageSource.getMessage("buttonName.owner.editInn"), EDIT_INN),
                entry(messageSource.getMessage("buttonName.owner.editEmail"), EDIT_EMAIL),
                entry(messageSource.getMessage("buttonName.owner.getPhoto"), PREPARE_GET_PHOTO),
                entry(messageSource.getMessage("buttonName.owner.backToApplications"), GET_APPLICATIONS)
        );
    }
}
