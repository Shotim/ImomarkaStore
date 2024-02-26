package com.external.inomarkastore.config.client;

import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class ClientMessageExecutionServicesConfig {

    @Bean("messageExecutionServicesByClientState")
    public Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState(
            List<MessageExecutionService> messageExecutionServiceList) {
        return messageExecutionServiceList.stream()
                .collect(toMap(MessageExecutionService::getState, identity()));
    }
}
