package com.external.imomarkastore.telegramapi.message;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface MessageExecutionService {

    ClientState getState();

    void execute(Update update, ClientInfo clientInfo) throws TelegramApiException;

    void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException;
}
