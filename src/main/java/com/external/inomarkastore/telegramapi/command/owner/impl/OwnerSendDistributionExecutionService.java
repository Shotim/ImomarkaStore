package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.google.gson.JsonArray;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.SEND_DISTRIBUTION;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getPhotoFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerSendDistributionExecutionService implements OwnerActionExecuteService {

    private static final String RECEIVED_SEND_DISTRIBUTION_MESSAGE_IDS = "receivedSendDistributionMessageIds";
    private final ClientInfoService clientInfoService;
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;


    @Override
    public String getCommand() {
        return SEND_DISTRIBUTION.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        if (jsonDataObject.has(RECEIVED_SEND_DISTRIBUTION_MESSAGE_IDS)) {
            jsonDataObject.getAsJsonArray(RECEIVED_SEND_DISTRIBUTION_MESSAGE_IDS).add(messageIdFromUpdate);
        } else {
            final var messageIds = new JsonArray();
            messageIds.add(messageIdFromUpdate);
            jsonDataObject.add(RECEIVED_SEND_DISTRIBUTION_MESSAGE_IDS, messageIds);
        }
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        final var telegramUserIds = clientInfoService.getTelegramUserIds();
        final var message = update.getMessage();
        for (Long telegramUserId : telegramUserIds) {
            if (message.hasPhoto()) {
                final var photoSizeOptional = getPhotoFromUpdate(update);
                if (photoSizeOptional.isPresent()) {
                    final var text = message.getCaption();
                    final var fileId = photoSizeOptional.get().getFileId();
                    final var sendPhoto = createSendPhotoForUser(telegramUserId, text, fileId);
                    inomarkaStore.execute(sendPhoto);
                } else {
                    final var text = message.getCaption();
                    final var sendMessage = createTextMessageForUser(telegramUserId, text);
                    inomarkaStore.execute(sendMessage);
                }
            } else if (message.hasText()) {
                final var text = message.getText();
                final var sendMessage = createTextMessageForUser(telegramUserId, text);
                inomarkaStore.execute(sendMessage);
            }
        }
    }
}
