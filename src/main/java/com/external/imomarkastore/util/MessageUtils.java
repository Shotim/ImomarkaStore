package com.external.imomarkastore.util;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;

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
        final var buttonNames = List.of(
                MESSAGE_SOURCE.getMessage("buttonName.client.newApplication"),
                MESSAGE_SOURCE.getMessage("buttonName.client.editName"),
                MESSAGE_SOURCE.getMessage("buttonName.client.editPhoneNumber"),
                MESSAGE_SOURCE.getMessage("buttonName.client.getCars"));
        return createTextMessageForUserWithReplyKeyBoardMarkup(user, text, buttonNames);
    }

    public static SendMessage createTextMessageForUserWithInlineButton(User user, String text, String buttonText, String buttonCallbackData) {
        return createTextMessageForUserWithInlineButtons(user, text, Map.of(buttonText, buttonCallbackData));
    }

    public static SendMessage createTextMessageForUserWithInlineButtons(User user, String text, Map<String, String> buttonTextToCallBackData) {
        final var inlineKeyBoardMarkup = createInlineKeyBoardMarkup(buttonTextToCallBackData);
        return SendMessage.builder()
                .text(text)
                .replyMarkup(inlineKeyBoardMarkup)
                .chatId(user.getId().toString())
                .build();
    }

    public static SendMessage createTextMessageForUserWithReplyKeyBoardMarkup(User user, String text, List<String> buttonNames) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardRows = buttonNames.stream().map(MessageUtils::createKeyBoardRowWithOneButton).toList();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return SendMessage.builder()
                .text(text)
                .chatId(user.getId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .build();
    }

    public static InlineKeyboardMarkup createInlineKeyBoardMarkup(Map<String, String> buttonTextToCallBackData) {
        final var replyMarkup = new InlineKeyboardMarkup();
        final var inlineButtons = buttonTextToCallBackData.entrySet().stream().map(entry -> {
            final var button = new InlineKeyboardButton();
            button.setText(entry.getKey());
            button.setCallbackData(entry.getValue());
            return List.of(button);
        }).toList();
        replyMarkup.setKeyboard(inlineButtons);
        return replyMarkup;
    }

    private static KeyboardRow createKeyBoardRowWithOneButton(String buttonText) {
        final var keyboardRow = new KeyboardRow();
        final var keyboardButton =
                new KeyboardButton(buttonText);
        keyboardRow.add(keyboardButton);
        return keyboardRow;
    }

    public static DeleteMessage createDeleteMessageForUser(User user, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(user.getId().toString())
                .messageId(messageId)
                .build();
    }

    public static AnswerCallbackQuery createAnswerCallbackQuery(String callbackId, String text) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text(text)
                .build();
    }
}
