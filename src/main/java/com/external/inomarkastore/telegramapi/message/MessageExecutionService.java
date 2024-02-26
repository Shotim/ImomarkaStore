package com.external.inomarkastore.telegramapi.message;

import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface MessageExecutionService {

    ClientState getState();

    void execute(Update update, ClientInfo clientInfo) throws TelegramApiException;

    void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException;
}
