package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.ClientState.EDIT_PHONE_NUMBER;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class EditPhoneNumberExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return EDIT_PHONE_NUMBER;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var phoneNumber = isBlank(clientInfo.getPhoneNumber()) ? EMPTY : clientInfo.getPhoneNumber();
        final var text = messageSource.getMessage("changePhoneNumber",
                List.of(phoneNumber).toArray());
        clientInfo.setState(EDIT_PHONE_NUMBER);
        clientInfoService.update(clientInfo);
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
