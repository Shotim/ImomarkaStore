package com.external.imomarkastore.config.owner;

import com.external.imomarkastore.constant.OwnerState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.DELETE_APPLICATION;
import static com.external.imomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_BLACK_LIST;
import static com.external.imomarkastore.constant.OwnerState.GET_CLIENTS;
import static com.external.imomarkastore.constant.OwnerState.GET_CLIENT_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.imomarkastore.constant.OwnerState.MOVE_CLIENT_BACK_FROM_BLACK_LIST;
import static com.external.imomarkastore.constant.OwnerState.MOVE_CLIENT_TO_BLACK_LIST;
import static com.external.imomarkastore.constant.OwnerState.RESTORE_APPLICATION;
import static com.external.imomarkastore.constant.OwnerState.START;
import static java.util.Map.entry;

@Configuration
public class OwnerStateMatrixConfig {

    @Bean("ownerStateMatrix")
    public Map<OwnerState, List<OwnerState>> createOwnerStateMatrix() {
        return Map.ofEntries(
                entry(START,
                        List.of(MAIN_MENU)),
                entry(MAIN_MENU,
                        List.of(GET_APPLICATIONS, GET_ARCHIVED_APPLICATIONS, GET_CLIENTS, GET_BLACK_LIST)),
                entry(GET_APPLICATIONS,
                        List.of(EXPORT_APPLICATIONS, MOVE_APPLICATION_TO_ARCHIVE, BACK_TO_MAIN_MENU)),
                entry(GET_ARCHIVED_APPLICATIONS,
                        List.of(DELETE_APPLICATION, RESTORE_APPLICATION, BACK_TO_MAIN_MENU)),
                entry(GET_CLIENTS,
                        List.of(GET_CLIENT_APPLICATIONS, MOVE_CLIENT_TO_BLACK_LIST, BACK_TO_MAIN_MENU)),
                entry(GET_BLACK_LIST,
                        List.of(MOVE_CLIENT_BACK_FROM_BLACK_LIST, BACK_TO_MAIN_MENU)),
                entry(GET_CLIENT_APPLICATIONS,
                        List.of(GET_CLIENTS, BACK_TO_MAIN_MENU))
        );
    }
}
