package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.constant.ClientState.SAVE_NAME;
import static com.external.imomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    public void execute(Update update, ClientInfo clientInfo) {
        final var text = getTextFromUpdate(update);
        clientInfo.setName(text);
        clientInfo.setState(MAIN_MENU);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var user = getUserFromUpdate(update);
        final var text = messageSource.getMessage("nameSavedSuccessfully");
        final var message = createClientTextMessageWithReplyKeyboardForMainMenu(user, text);
        inomarkaStore.execute(message);
    }
}
