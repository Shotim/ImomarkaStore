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

import static com.external.inomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.inomarkastore.constant.ClientState.SAVE_NAME;
import static com.external.inomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class SaveNameExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return SAVE_NAME;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var text = getTextFromUpdate(update);
        clientInfo.setName(text);
        clientInfo.setState(MAIN_MENU);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var text = messageSource.getMessage("nameSavedSuccessfully");
        final var message = createClientTextMessageWithReplyKeyboardForMainMenu(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
