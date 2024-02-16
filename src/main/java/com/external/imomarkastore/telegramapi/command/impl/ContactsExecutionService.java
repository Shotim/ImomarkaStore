package com.external.imomarkastore.telegramapi.command.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
public class ContactsExecutionService extends StateMessagesExecutionService implements CommandExecutionService {
    private final InomarkaStore inomarkaStore;
    private final ClientInfoService clientInfoService;
    private final BotMessageSource messageSource;

    protected ContactsExecutionService(
            @Qualifier("messageExecutionServicesByClientState")
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState,
            InomarkaStore inomarkaStore,
            ClientInfoService clientInfoService, BotMessageSource messageSource) {
        super(messageExecutionServicesByClientState);
        this.inomarkaStore = inomarkaStore;
        this.clientInfoService = clientInfoService;
        this.messageSource = messageSource;
    }

    @Override
    public String getCommand() {
        return "/contacts";
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var outputText = messageSource.getMessage("shopInfo");
        final var user = getUserFromUpdate(update);
        final var outputMessage = createTextMessageForUser(user, outputText);
        inomarkaStore.execute(outputMessage);
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(user.getId());
        super.sendMessages(update, clientInfoOptional.orElseGet(ClientInfo::new));
    }
}
