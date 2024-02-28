package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.ClientState.INSERT_COMMENT;
import static com.external.inomarkastore.constant.ClientState.INSERT_MAIN_PURPOSE;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.inomarkastore.util.UpdateUtils.getPhotoFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class InsertMainPurposeExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return INSERT_MAIN_PURPOSE;
    }

    @Override
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var photoOptional = getPhotoFromUpdate(update);
        clientInfo.setState(INSERT_MAIN_PURPOSE);
        clientInfoService.update(clientInfo);
        final var application = applicationService
                .getFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
        if (photoOptional.isPresent()) {
            application.setMainPurpose(messageSource.getMessage("onPhoto"));
            application.setMainPurposePhotoId(photoOptional.get().getFileId());
        } else {
            final var text = getTextFromUpdate(update);
            application.setMainPurpose(text);
        }
        applicationService.update(application);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = getUserFromUpdate(update);

        final var text = messageSource.getMessage("insertComment");
        final var application = applicationService
                .getFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
        final var message = createTextMessageForUserWithInlineButton(user.getId(), text,
                messageSource.getMessage("buttonName.client.skipInsertComment"),
                "%s:%s".formatted(INSERT_COMMENT.name(), application.getId()));
        final var messageId = inomarkaStore.execute(message).getMessageId();
        final var jsonObject = new JsonObject();
        jsonObject.add("messageId", new JsonPrimitive(messageId));
        clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
        clientInfoService.update(clientInfo);
    }
}