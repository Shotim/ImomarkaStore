package com.external.imomarkastore.config.client;

import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class ClientCommandExecutionServicesConfig {

    @Bean("clientCommandExecutionServicesByCommands")
    public Map<String, CommandExecutionService> messageExecutionServicesByCommands(
            List<CommandExecutionService> commandExecutionServiceList) {
        return commandExecutionServiceList.stream()
                .collect(toMap(CommandExecutionService::getCommand, identity()));
    }
}
