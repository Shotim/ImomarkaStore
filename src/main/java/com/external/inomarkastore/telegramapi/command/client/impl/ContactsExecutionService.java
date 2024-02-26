package com.external.inomarkastore.telegramapi.command.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.CommandExecutionService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
public class ContactsExecutionService extends StateMessagesExecutionService implements CommandExecutionService {
    private final InomarkaStore inomarkaStore;
    private final ClientInfoService clientInfoService;
    private final OwnerInfoService ownerInfoService;

    protected ContactsExecutionService(
            @Qualifier("messageExecutionServicesByClientState")
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState,
            InomarkaStore inomarkaStore,
            ClientInfoService clientInfoService, OwnerInfoService ownerInfoService) {
        super(messageExecutionServicesByClientState);
        this.inomarkaStore = inomarkaStore;
        this.clientInfoService = clientInfoService;
        this.ownerInfoService = ownerInfoService;
    }

    @Override
    public String getCommand() {
        return "/contacts";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var outputText = ownerInfoService.createContactsPayload();
        final var user = getUserFromUpdate(update);
        final var outputMessage = createTextMessageForUser(user.getId(), outputText);
        inomarkaStore.execute(outputMessage);
        try {
            final var clientInfo = clientInfoService.getByTelegramUserId(user.getId());
            super.sendMessages(update, clientInfo);
        } catch (BusinessLogicException exception) {
            super.sendMessages(update, new ClientInfo());
        }
    }
}
