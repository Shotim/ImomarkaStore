package com.external.imomarkastore.config;

import com.external.imomarkastore.util.BotMessageSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    public static BotMessageSource getBotMessageSource() {
        return appContext.getBean(BotMessageSource.class);
    }

    public static String getBotPaymentToken() {
        return appContext.getEnvironment().getProperty("bot.payment_token");
    }
}
