package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Stream;

import static com.external.inomarkastore.constant.ClientState.INITIAL_SET_PHONE_NUMBER;
import static com.external.inomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.inomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.inomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;
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
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        try {
            final var text = getTextFromUpdate(update);
            final String formattedPhoneNumber = formatAndValidatePhoneNumber(text);
            final var clientInfoOptional = clientInfoService.getByPhoneNumberOpt(formattedPhoneNumber);
            if (clientInfoOptional.isPresent()) {
                final var recentClientInfo = clientInfoOptional.get();
                recentClientInfo.setName(clientInfo.getName());
                recentClientInfo.setTelegramUserName(clientInfo.getTelegramUserName());
                recentClientInfo.setTelegramUserId(clientInfo.getTelegramUserId());
                recentClientInfo.setState(MAIN_MENU);
                clientInfoService.update(recentClientInfo);
                clientInfoService.deleteById(clientInfo.getId());
            } else {
                clientInfo.setPhoneNumber(formattedPhoneNumber);
                clientInfo.setState(MAIN_MENU);
                clientInfoService.update(clientInfo);
            }
            final var updatedClientInfo = clientInfoService.update(clientInfo);
            sendMessages(update, updatedClientInfo);
        } catch (IllegalArgumentException exception) {
            final var user = getUserFromUpdate(update);
            final var text = messageSource.getMessage("error.wrongPhoneNumberFormat");
            final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
            inomarkaStore.execute(message);
        }
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {

        final var outputText = messageSource.getMessage("template.client.clientInfo",
                Stream.of(clientInfo.getName(), clientInfo.getPhoneNumber())
                        .map(string -> isBlank(string) ? EMPTY : string).toArray());
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUser(user.getId(), outputText);
        inomarkaStore.execute(message);
        final var text = "Теперь Вы можете создать запрос, который Вас интересует.";
        final var sendMessage = createClientTextMessageWithReplyKeyboardForMainMenu(user.getId(), text);
        inomarkaStore.execute(sendMessage);
    }
}
