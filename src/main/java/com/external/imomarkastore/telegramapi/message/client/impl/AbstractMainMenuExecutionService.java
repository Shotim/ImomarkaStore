package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.external.imomarkastore.util.UpdateUtils;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;

@RequiredArgsConstructor
public abstract class AbstractMainMenuExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        clientInfo.setState(MAIN_MENU);
        clientInfo.setAdditionalJsonDataForNextOperations(null);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = UpdateUtils.getUserFromUpdate(update);
        final var text = messageSource.getMessage("youReturnedBackToMainMenu");
        final var message = createClientTextMessageWithReplyKeyboardForMainMenu(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
