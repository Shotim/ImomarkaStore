package com.external.imomarkastore.telegramapi.message;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageExecutionService {

    ClientState getState();

    void execute(Update update, ClientInfo clientInfo);

    void sendMessages(Update update, ClientInfo clientInfo);
}
