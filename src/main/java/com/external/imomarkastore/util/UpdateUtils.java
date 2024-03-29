package com.external.imomarkastore.util;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class UpdateUtils {

    public static User getUserFromUpdate(Update update) {
        return update.hasCallbackQuery() ?
                update.getCallbackQuery().getFrom() :
                update.getMessage().getFrom();
    }

    public static String getTextFromUpdate(Update update) {
        return update.getMessage().getText();
    }

    public static UUID getIdFromCallbackData(Update update) {
        final var data = update.getCallbackQuery().getData();
        final var id = data.substring(data.indexOf(":") + 1);
        return UUID.fromString(id);
    }

    public static Optional<PhotoSize> getPhotoFromUpdate(Update update) {
        final var photoSizes = update.getMessage().getPhoto();
        return isNull(photoSizes) ?
                Optional.empty() :
                photoSizes
                        .stream()
                        .max(comparingInt(PhotoSize::getFileSize));
    }
}
