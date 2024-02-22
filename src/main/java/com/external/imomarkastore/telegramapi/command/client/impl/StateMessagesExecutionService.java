package com.external.imomarkastore.telegramapi.command.client.impl;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static java.util.Objects.nonNull;

public abstract class StateMessagesExecutionService {
    private final Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState;

    protected StateMessagesExecutionService(
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState) {
        this.messageExecutionServicesByClientState = messageExecutionServicesByClientState;
    }

    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var state = clientInfo.getState();
        final var messageExecutionService = messageExecutionServicesByClientState.get(state);
        if (nonNull(messageExecutionService)) {
            messageExecutionService.sendMessages(update, clientInfo);
        }
    }
}
