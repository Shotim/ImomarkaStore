package com.external.inomarkastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static org.apache.commons.codec.CharEncoding.UTF_8;

@SpringBootApplication
public class InomarkaStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(InomarkaStoreApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(InomarkaStore bot) throws TelegramApiException {
        final var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
        return telegramBotsApi;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {

        var source = new ResourceBundleMessageSource();
        source.setBasename("i18n/i18n");
        source.setDefaultEncoding(UTF_8);
        source.setUseCodeAsDefaultMessage(true);

        return source;
    }
}
