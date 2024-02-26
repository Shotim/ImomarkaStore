package com.external.inomarkastore.telegramapi.command.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.command.CommandExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.ClientState.REPEATED_START;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.http.util.TextUtils.isBlank;

@Service
@RequiredArgsConstructor
public class StartExecutionService implements CommandExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var telegramUserId = user.getId();
        try {
            final var clientInfo = clientInfoService.getByTelegramUserId(telegramUserId);
            sendRepeatedStartMessages(clientInfo);
        } catch (BusinessLogicException exception) {
            clientInfoService.create(telegramUserId, user.getUserName());
            sendStartMessages(telegramUserId);
        }
    }

    private void sendRepeatedStartMessages(ClientInfo clientInfo) throws TelegramApiException {
        clientInfo.setState(REPEATED_START);
        final var clientName = isBlank(clientInfo.getName()) ?
                messageSource.getMessage("nameReplacement") :
                clientInfo.getName();
        final var welcomeText = messageSource.getMessage("repeatedWelcomeMessage",
                List.of(clientName).toArray());
        final var helloAgainMessage = createTextMessageForUser(clientInfo.getTelegramUserId(), welcomeText);
        inomarkaStore.execute(helloAgainMessage);
    }

    private void sendStartMessages(Long telegramUserId) throws TelegramApiException {
        final var welcomeText = messageSource.getMessage("welcomeMessage");
        final var helloMessage = createTextMessageForUser(telegramUserId, welcomeText);
        inomarkaStore.execute(helloMessage);
        final var setNameText = messageSource.getMessage("setNameText");
        final var setNameMessage = createTextMessageForUser(telegramUserId, setNameText);
        inomarkaStore.execute(setNameMessage);
    }
}
