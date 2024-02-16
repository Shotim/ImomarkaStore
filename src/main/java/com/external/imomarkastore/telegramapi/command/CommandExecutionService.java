package com.external.imomarkastore.telegramapi.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandExecutionService {

    String getCommand();

    void execute(Update update);
}
