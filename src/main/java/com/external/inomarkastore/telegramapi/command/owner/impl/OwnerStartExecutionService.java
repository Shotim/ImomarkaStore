package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.inomarkastore.constant.OwnerState.START;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    public void execute(Update update) throws TelegramApiException {
        ownerInfoService.updateState(MAIN_MENU);
        final var user = getUserFromUpdate(update);

        final var welcome = messageSource.getMessage("owner.welcome");
        final var textMessageForUser = createTextMessageForUser(user.getId(), welcome);
        inomarkaStore.execute(textMessageForUser);

        final var startWork = messageSource.getMessage("owner.startWork");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.getApplications"),
                messageSource.getMessage("buttonName.owner.getArchivedApplications"),
                messageSource.getMessage("buttonName.owner.getClients"),
                messageSource.getMessage("buttonName.owner.getBlackList"),
                messageSource.getMessage("buttonName.owner.getContacts"),
                messageSource.getMessage("buttonName.owner.getPhoto"),
                messageSource.getMessage("buttonName.owner.prepareDistribution")
        );
        final var startWorkMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), startWork, buttonNames);
        inomarkaStore.execute(startWorkMessage);
    }
}
