package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.PREPARE_GET_PHOTO;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    public void execute(Update update) {
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
