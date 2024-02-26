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

import static com.external.inomarkastore.constant.OwnerState.PREPARE_GET_PHOTO;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerPrepareGetPhotoExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return PREPARE_GET_PHOTO.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        ownerInfoService.updateState(PREPARE_GET_PHOTO);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonDataObject.addProperty("receivedPrepareGetPhotoMessageId", messageIdFromUpdate);
        final var text = messageSource.getMessage("owner.prepareGetPhoto");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var textMessageForUser = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), text, buttonNames);
        final var messageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        jsonDataObject.addProperty("prepareGetPhotoMessageId", messageId);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }
}
