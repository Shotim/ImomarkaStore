package com.external.inomarkastore.util;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = PRIVATE)
public class UpdateUtils {

    public static User getUserFromUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom();
        } else {
            return update.getMessage().getFrom();
        }
    }

    public static String getTextFromUpdate(Update update) {
        final var text = update.getMessage().getText();
        return isNull(text) ? EMPTY : text;
    }

    public static UUID getUUIDIdFromCallbackDataFromUpdate(Update update) {
        final var data = getCallbackDataFromUpdate(update);
        final var id = data.substring(data.indexOf(":") + 1);
        return UUID.fromString(id);
    }

    public static Long getLongIdFromCallbackDataFromUpdate(Update update) {
        final var data = getCallbackDataFromUpdate(update);
        final var id = data.substring(data.indexOf(":") + 1);
        return Long.valueOf(id);
    }

    public static String getCallbackDataFromUpdate(Update update) {
        return update.getCallbackQuery().getData();
    }

    public static Optional<PhotoSize> getPhotoFromUpdate(Update update) {
        final var photoSizes = update.getMessage().getPhoto();
        return isNull(photoSizes) ?
                Optional.empty() :
                photoSizes
                        .stream()
                        .max(comparingInt(PhotoSize::getFileSize));
    }

    public static Optional<PhotoSize> getPhotoFromMessage(Message message) {
        final var photoSizes = message.getPhoto();
        return isNull(photoSizes) ?
                Optional.empty() :
                photoSizes
                        .stream()
                        .max(comparingInt(PhotoSize::getFileSize));
    }

    public static Integer getMessageIdFromUpdate(Update update) {
        return update.getMessage().getMessageId();
    }

    public static String getCallbackIdFromUpdate(Update update) {
        return update.getCallbackQuery().getId();
    }
}
