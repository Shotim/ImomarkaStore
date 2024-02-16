package com.external.imomarkastore.telegramapi.command.client.impl;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.command.client.common.ApplicationsSendHelper;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Collections.emptyList;

@Service
public class ApplicationsExecutionService extends StateMessagesExecutionService implements CommandExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final BotMessageSource messageSource;
    private final ApplicationsSendHelper applicationsSendHelper;

    protected ApplicationsExecutionService(
            @Qualifier("messageExecutionServicesByClientState")
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState,
            ClientInfoService clientInfoService,
            ApplicationService applicationService,
            BotMessageSource messageSource,
            ApplicationsSendHelper applicationsSendHelper) {
        super(messageExecutionServicesByClientState);
        this.clientInfoService = clientInfoService;
        this.applicationService = applicationService;
        this.messageSource = messageSource;
        this.applicationsSendHelper = applicationsSendHelper;
    }

    @Override
    public String getCommand() {
        return "/applications";
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(user.getId());
        final List<Application> applications = clientInfoOptional.isEmpty() ?
                emptyList() :
                applicationService.getFullyCreatedApplicationsForClient(clientInfoOptional.get());

        final var applicationsEmpty = applications.isEmpty();
        final var text = messageSource.getMessage(applicationsEmpty ?
                "youDoNotHaveActiveApplications" :
                "yourActiveApplications");
        applicationsSendHelper.sendApplications(user, applications, text);
        super.sendMessages(update, clientInfoOptional.orElseGet(ClientInfo::new));
    }
}
