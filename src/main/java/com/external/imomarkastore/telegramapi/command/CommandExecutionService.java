package com.external.imomarkastore.telegramapi.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandExecutionService {

    String getCommand();

    void execute(Update update) throws TelegramApiException;
}
