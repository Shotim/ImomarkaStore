package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.ClientState.EDIT_NAME;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class EditNameExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return EDIT_NAME;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var clientInfoName = isBlank(clientInfo.getName()) ? EMPTY : clientInfo.getName();
        final var text = messageSource.getMessage("changeName",
                List.of(clientInfoName).toArray());
        clientInfo.setState(EDIT_NAME);
        clientInfoService.update(clientInfo);
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
