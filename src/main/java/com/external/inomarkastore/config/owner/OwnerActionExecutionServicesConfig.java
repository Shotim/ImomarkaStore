package com.external.inomarkastore.config.owner;

import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class OwnerActionExecutionServicesConfig {

    @Bean("ownerActionExecutionServicesByStateName")
    public Map<String, OwnerActionExecuteService> ownerActionExecutionServicesByStateName(
            List<OwnerActionExecuteService> ownerActionExecuteServices) {
        return ownerActionExecuteServices.stream()
                .collect(toMap(OwnerActionExecuteService::getCommand, identity()));
    }
}
