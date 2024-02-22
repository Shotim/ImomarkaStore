package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.imomarkastore.constant.ClientState.INITIAL_SET_NAME;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialSetNameExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return INITIAL_SET_NAME;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var text = getTextFromUpdate(update);
        clientInfo.setName(text);
        clientInfo.setState(INITIAL_SET_NAME);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var text = messageSource.getMessage("initialSetPhoneNumber");
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUser(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
