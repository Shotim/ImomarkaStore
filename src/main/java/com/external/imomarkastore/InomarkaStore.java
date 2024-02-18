package com.external.imomarkastore;

import com.external.imomarkastore.exception.BotException;
import com.external.imomarkastore.service.OwnerInfoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Component
public class InomarkaStore extends TelegramLongPollingBot {

    private final String botName;
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStoreClient inomarkaStoreClient;
    private final InomarkaStoreOwner inomarkaStoreOwner;

    public InomarkaStore(@Value("${bot.token}") String botToken,
                         OwnerInfoService ownerInfoService,
                         @Value("${bot.name}") String botName, InomarkaStoreClient inomarkaStoreClient, InomarkaStoreOwner inomarkaStoreOwner) {
        super(botToken);
        this.botName = botName;
        this.ownerInfoService = ownerInfoService;
        this.inomarkaStoreClient = inomarkaStoreClient;
        this.inomarkaStoreOwner = inomarkaStoreOwner;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        final var user = getUserFromUpdate(update);
        try {
            if (ownerInfoService.isOwner(user.getId())) {
                inomarkaStoreOwner.processAction(update);
            } else {
                inomarkaStoreClient.processAction(update, user);
            }
        } catch (BotException botException) {
            this.execute(botException.getErrorMessage());
        }
    }
}
