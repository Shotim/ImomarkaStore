package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.INSERT_COMMENT;
import static com.external.imomarkastore.constant.ClientState.INSERT_MAIN_PURPOSE;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.imomarkastore.util.UpdateUtils.getPhotoFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
        final var photoOptional = getPhotoFromUpdate(update);
        clientInfo.setState(INSERT_MAIN_PURPOSE);
        clientInfoService.update(clientInfo);
        final var application = applicationService
                .findFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
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
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var user = getUserFromUpdate(update);

        final var text = messageSource.getMessage("insertComment");
        final var application = applicationService
                .findFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
        final var message = createTextMessageForUserWithInlineButton(user, text,
                messageSource.getMessage("buttonName.client.skipInsertComment"),
                "%s:%s".formatted(INSERT_COMMENT.name(), application.getId()));
        final var messageId = inomarkaStore.execute(message).getMessageId();
        final var jsonObject = new JsonObject();
        jsonObject.add("messageId", new JsonPrimitive(messageId));
        clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
        clientInfoService.update(clientInfo);
    }
}
