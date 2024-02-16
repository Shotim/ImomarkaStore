package com.external.imomarkastore.util;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static com.external.imomarkastore.config.ApplicationContextHolder.getBotMessageSource;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class MessageUtils {

    private static final BotMessageSource MESSAGE_SOURCE = getBotMessageSource();


    public static SendMessage createTextMessageForUser(User user, String text) {
        return SendMessage.builder()
                .chatId(user.getId().toString())
                .text(text)
                .build();
    }

    public static SendMessage createTextMessageForUserWithRemoveKeyBoard(User user, String text) {
        return SendMessage.builder()
                .chatId(user.getId().toString())
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    public static SendMessage createTextMessageWithReplyKeyboardForMainMenu(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardButtonsNewApplication = new KeyboardRow();
        final var keyboardButtonsEditName = new KeyboardRow();
        final var keyboardButtonsEditPhoneNumber = new KeyboardRow();
        final var keyboardButtonsGetCars = new KeyboardRow();
        final var keyboardButtonNewApplication = new KeyboardButton(MESSAGE_SOURCE.getMessage("buttonName.newApplication"));
        final var keyboardButtonEditName = new KeyboardButton(MESSAGE_SOURCE.getMessage("buttonName.editName"));
        final var keyboardButtonEditPhoneNumber = new KeyboardButton(MESSAGE_SOURCE.getMessage("buttonName.editPhoneNumber"));
        final var keyboardButtonGetCars = new KeyboardButton(MESSAGE_SOURCE.getMessage("buttonName.getCars"));
        keyboardButtonsNewApplication.add(keyboardButtonNewApplication);
        keyboardButtonsEditName.add(keyboardButtonEditName);
        keyboardButtonsEditPhoneNumber.add(keyboardButtonEditPhoneNumber);
        keyboardButtonsGetCars.add(keyboardButtonGetCars);
        replyKeyboardMarkup.setKeyboard(
                List.of(keyboardButtonsNewApplication,
                        keyboardButtonsEditName,
                        keyboardButtonsEditPhoneNumber,
                        keyboardButtonsGetCars));
        return SendMessage.builder()
                .text(text)
                .replyMarkup(replyKeyboardMarkup)
                .chatId(user.getId().toString())
                .build();
    }

    public static SendMessage createTextMessageWithInlineButton(User user, String text, String buttonText, String buttonCallbackData) {
        final var replyMarkup = new InlineKeyboardMarkup();
        final var keyboardButton = new InlineKeyboardButton();
        keyboardButton.setText(buttonText);
        keyboardButton.setCallbackData(buttonCallbackData);
        replyMarkup.setKeyboard(List.of(List.of(keyboardButton)));
        return SendMessage.builder()
                .text(text)
                .replyMarkup(replyMarkup)
                .chatId(user.getId().toString())
                .build();
    }

    public static SendMessage createTextMessageWithButtonBackToMainMenu(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardRow = new KeyboardRow();
        final var keyboardButton = new KeyboardButton();
        keyboardButton.setText(MESSAGE_SOURCE.getMessage("buttonName.backToMainMenu"));
        keyboardRow.add(keyboardButton);
        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        return SendMessage.builder()
                .chatId(user.getId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text(text)
                .build();
    }
}
