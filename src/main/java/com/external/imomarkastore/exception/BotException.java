package com.external.imomarkastore.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
public class BotException extends RuntimeException {
    @Getter
    private final transient SendMessage errorMessage;
}
