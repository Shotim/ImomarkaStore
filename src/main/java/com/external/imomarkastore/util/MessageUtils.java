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

    public static SendMessage createClientTextMessageWithReplyKeyboardForMainMenu(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();

        final var keyboardButtonsNewApplication = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.client.newApplication"));

        final var keyboardButtonsEditName = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.client.editName"));

        final var keyboardButtonsEditPhoneNumber = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.client.editPhoneNumber"));

        final var keyboardButtonsGetCars = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.client.getCars"));

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

    public static SendMessage createOwnerTextMessageWithReplyKeyBoardForMainMenu(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();

        final var keyboardButtonsGetApplications = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.owner.getApplications"));

        final var keyboardButtonsGetArchivedApplications = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.owner.getArchivedApplications"));

        final var keyboardButtonsGetClients = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.owner.getClients"));

        final var keyboardButtonsGetBlackList = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.owner.getBlackList"));

        replyKeyboardMarkup.setKeyboard(List.of(
                keyboardButtonsGetApplications,
                keyboardButtonsGetArchivedApplications,
                keyboardButtonsGetClients,
                keyboardButtonsGetBlackList
        ));
        return SendMessage.builder()
                .text(text)
                .replyMarkup(replyKeyboardMarkup)
                .chatId(user.getId().toString())
                .build();
    }

    public static SendMessage createTextMessageWithInlineButton(User user, String text, String buttonText, String buttonCallbackData) {
        final var replyMarkup = createInlineKeyboardMarkup(buttonText, buttonCallbackData);
        return SendMessage.builder()
                .text(text)
                .replyMarkup(replyMarkup)
                .chatId(user.getId().toString())
                .build();
    }

    public static InlineKeyboardMarkup createInlineKeyboardMarkup(String buttonText, String buttonCallbackData) {
        final var replyMarkup = new InlineKeyboardMarkup();
        final var keyboardButton = new InlineKeyboardButton();
        keyboardButton.setText(buttonText);
        keyboardButton.setCallbackData(buttonCallbackData);
        replyMarkup.setKeyboard(List.of(List.of(keyboardButton)));
        return replyMarkup;
    }

    public static SendMessage createTextMessageWithButtonBackToMainMenuForClient(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardRow = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.client.backToMainMenu"));
        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        return SendMessage.builder()
                .chatId(user.getId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text(text)
                .build();
    }

    public static SendMessage createTextMessageWithButtonBackToMainMenuForOwner(User user, String text) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardRow = createKeyBoardRowWithOneButton(
                MESSAGE_SOURCE.getMessage("buttonName.owner.backToMainMenu"));

        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        return SendMessage.builder()
                .chatId(user.getId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text(text)
                .build();
    }

    private static KeyboardRow createKeyBoardRowWithOneButton(String buttonText) {
        final var keyboardRow = new KeyboardRow();
        final var keyboardButton =
                new KeyboardButton(buttonText);
        keyboardRow.add(keyboardButton);
        return keyboardRow;
    }
}
