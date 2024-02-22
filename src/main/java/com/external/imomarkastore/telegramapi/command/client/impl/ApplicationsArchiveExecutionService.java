package com.external.imomarkastore.telegramapi.command.client.impl;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.exception.BusinessLogicException;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.command.client.common.ApplicationsSendHelper;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Collections.emptyList;

@Service
public class ApplicationsArchiveExecutionService extends StateMessagesExecutionService implements CommandExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final BotMessageSource messageSource;
    private final ApplicationsSendHelper applicationsSendHelper;

    protected ApplicationsArchiveExecutionService(
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
        return "/archive";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        try {
            final var clientInfo = clientInfoService.getByTelegramUserId(user.getId());
            final var applications = applicationService.getArchivedApplicationsForClient(clientInfo);
            final var text = messageSource.getMessage("client.yourArchiveApplications");
            applicationsSendHelper.sendApplications(user.getId(), applications, text);
            super.sendMessages(update, clientInfo);
        } catch (BusinessLogicException exception) {
            final var text = messageSource.getMessage("client.youDoNotHaveArchiveApplications");
            applicationsSendHelper.sendApplications(user.getId(), emptyList(), text);
        }
    }
}
