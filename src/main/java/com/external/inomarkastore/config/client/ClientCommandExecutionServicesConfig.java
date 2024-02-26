package com.external.inomarkastore.config.client;

import com.external.inomarkastore.telegramapi.command.CommandExecutionService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
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
            List<CommandExecutionService> commandExecutionServiceList, List<OwnerActionExecuteService> ownerActionExecuteServices) {
        return commandExecutionServiceList.stream()
                .filter(commandExecutionService -> !ownerActionExecuteServices.contains(commandExecutionService))
                .collect(toMap(CommandExecutionService::getCommand, identity()));
    }
}
