package com.external.imomarkastore.telegramapi.command.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.exception.BusinessLogicException;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.ClientState.REPEATED_START;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
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
