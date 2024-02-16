package com.external.imomarkastore.config;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class MessageExecutionServicesConfig {

    @Bean("messageExecutionServicesByClientState")
    public Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState(
            List<MessageExecutionService> messageExecutionServiceList) {
        return messageExecutionServiceList.stream()
                .collect(toMap(MessageExecutionService::getState, identity()));
    }
}
