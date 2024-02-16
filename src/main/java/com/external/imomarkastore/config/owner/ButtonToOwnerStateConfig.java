package com.external.imomarkastore.config.owner;

import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_BLACK_LIST;
import static com.external.imomarkastore.constant.OwnerState.GET_CLIENTS;

@Configuration
@RequiredArgsConstructor
public class ButtonToOwnerStateConfig {

    private final BotMessageSource messageSource;

    @Bean("buttonToOwnerStateMatrix")
    public Map<String, OwnerState> buttonToOwnerStateMatrix() {
        return Map.of(messageSource.getMessage("buttonName.owner.getApplications"), GET_APPLICATIONS,
                messageSource.getMessage("buttonName.owner.getArchivedApplications"), GET_ARCHIVED_APPLICATIONS,
                messageSource.getMessage("buttonName.owner.getClients"), GET_CLIENTS,
                messageSource.getMessage("buttonName.owner.getBlackList"), GET_BLACK_LIST,
                messageSource.getMessage("buttonName.owner.exportApplications"), EXPORT_APPLICATIONS,
                messageSource.getMessage("buttonName.owner.backToMainMenu"), BACK_TO_MAIN_MENU);
    }
}
