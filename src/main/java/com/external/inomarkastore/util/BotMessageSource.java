package com.external.inomarkastore.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Component
@RequiredArgsConstructor
public class BotMessageSource {

    private final MessageSource messageSource;

    public String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, getLocale());
    }

    public String getMessage(String code) {
        return getMessage(code, null);
    }
}
