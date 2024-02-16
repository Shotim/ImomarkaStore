package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.START;
import static com.external.imomarkastore.util.MessageUtils.createOwnerTextMessageWithReplyKeyBoardForMainMenu;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerStartExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return START.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        ownerInfoService.updateState(MAIN_MENU);
        final var user = getUserFromUpdate(update);

        final var welcome = messageSource.getMessage("owner.welcome");
        final var textMessageForUser = createTextMessageForUser(user, welcome);
        inomarkaStore.execute(textMessageForUser);

        final var startWork = messageSource.getMessage("owner.startWork");
        final var startWorkMessage = createOwnerTextMessageWithReplyKeyBoardForMainMenu(user, startWork);
        inomarkaStore.execute(startWorkMessage);
    }
}