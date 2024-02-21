package com.external.imomarkastore.telegramapi.command.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

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
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var telegramUserId = user.getId();
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(telegramUserId);

        if (clientInfoOptional.isEmpty()) {
            clientInfoService.create(telegramUserId);
            final var welcomeText = messageSource.getMessage("welcomeMessage");
            final var helloMessage = createTextMessageForUser(user.getId(), welcomeText);
            inomarkaStore.execute(helloMessage);
            final var setNameText = messageSource.getMessage("setNameText");
            final var setNameMessage = createTextMessageForUser(user.getId(), setNameText);
            inomarkaStore.execute(setNameMessage);
        } else {
            final var clientInfo = clientInfoOptional.get();
            clientInfo.setState(REPEATED_START);
            final var clientName = isBlank(clientInfo.getName()) ?
                    messageSource.getMessage("nameReplacement") :
                    clientInfo.getName();
            final var welcomeText = messageSource.getMessage("repeatedWelcomeMessage",
                    List.of(clientName).toArray());
            final var helloAgainMessage = createTextMessageForUser(user.getId(), welcomeText);
            inomarkaStore.execute(helloAgainMessage);
        }
    }
}
