package com.external.imomarkastore.config.owner;

import com.external.imomarkastore.constant.OwnerState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.START;

@Configuration
public class CommandToStateConfig {

    @Bean("commandToOwnerStateMatrix")
    public Map<String, OwnerState> commandToOwnerStateMatrix() {
        return Map.of("/start", START,
                "/applications", GET_APPLICATIONS,
                "/archive", GET_ARCHIVED_APPLICATIONS,
                "/contacts", GET_CONTACTS);
    }
}