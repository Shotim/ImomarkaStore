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
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Stream;

import static com.external.imomarkastore.constant.ClientState.INITIAL_SET_PHONE_NUMBER;
import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.imomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class InitialSetPhoneNumberExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return INITIAL_SET_PHONE_NUMBER;
    }

    @Override
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
        try {
            final var text = getTextFromUpdate(update);
            final String formattedPhoneNumber = formatAndValidatePhoneNumber(text);
            clientInfo.setPhoneNumber(formattedPhoneNumber);
            clientInfo.setState(MAIN_MENU);
            final var updatedClientInfo = clientInfoService.update(clientInfo);
            sendMessages(update, updatedClientInfo);
        } catch (IllegalArgumentException exception) {
            final var user = getUserFromUpdate(update);
            final var text = messageSource.getMessage("error.wrongPhoneNumberFormat");
            final var message = createTextMessageForUserWithRemoveKeyBoard(user, text);
            inomarkaStore.execute(message);
        }
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {

        final var outputText = messageSource.getMessage("template.clientInfo",
                Stream.of(clientInfo.getName(), clientInfo.getPhoneNumber())
                        .map(string -> isBlank(string) ? EMPTY : string).toArray());
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUser(user, outputText);
        inomarkaStore.execute(message);
        final var text = "Теперь Вы можете создать запрос, который Вас интересует.";
        final var sendMessage = createClientTextMessageWithReplyKeyboardForMainMenu(user, text);
        inomarkaStore.execute(sendMessage);
    }
}
