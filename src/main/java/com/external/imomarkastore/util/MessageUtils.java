package com.external.imomarkastore.util;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
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

    public static SendMessage createTextMessageForUser(Long telegramUserId, String text) {
        return SendMessage.builder()
                .chatId(telegramUserId)
                .text(text)
                .build();
    }

    public static SendMessage createTextMessageForUserWithRemoveKeyBoard(Long telegramUserId, String text) {
        return SendMessage.builder()
                .chatId(telegramUserId)
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    public static SendMessage createClientTextMessageWithReplyKeyboardForMainMenu(Long telegramUserId, String text) {
        final var buttonNames = List.of(
                MESSAGE_SOURCE.getMessage("buttonName.client.newApplication"),
                MESSAGE_SOURCE.getMessage("buttonName.client.editName"),
                MESSAGE_SOURCE.getMessage("buttonName.client.editPhoneNumber"),
                MESSAGE_SOURCE.getMessage("buttonName.client.getCars"));
        return createTextMessageForUserWithReplyKeyBoardMarkup(telegramUserId, text, buttonNames);
    }

    public static SendMessage createTextMessageForUserWithInlineButton(Long telegramUserId, String text, String buttonText, String buttonCallbackData) {
        return createTextMessageForUserWithInlineButtons(telegramUserId, text, Map.of(buttonText, buttonCallbackData));
    }

    public static SendMessage createTextMessageForUserWithInlineButtons(Long telegramUserId, String text, Map<String, String> buttonTextToCallBackData) {
        final var inlineKeyBoardMarkup = createInlineKeyBoardMarkup(buttonTextToCallBackData);
        return SendMessage.builder()
                .text(text)
                .replyMarkup(inlineKeyBoardMarkup)
                .chatId(telegramUserId)
                .build();
    }

    public static SendMessage createTextMessageForUserWithReplyKeyBoardMarkup(Long telegramUserId, String text, List<String> buttonNames) {
        final var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final var keyboardRows = buttonNames.stream().map(MessageUtils::createKeyBoardRowWithOneButton).toList();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return SendMessage.builder()
                .text(text)
                .chatId(telegramUserId)
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

    public static DeleteMessage createDeleteMessageForUser(Long telegramUserId, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(telegramUserId)
                .messageId(messageId)
                .build();
    }

    public static AnswerCallbackQuery createAnswerCallbackQuery(String callbackId, String text) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text(text)
                .build();
    }

    public static SendInvoice createSendInvoice(Long telegramUserId, String title, String description, Integer priceValue, String priceLabel) {
        final var price = new LabeledPrice();
        price.setAmount(priceValue);
        price.setLabel(priceLabel);
        return SendInvoice.builder()
                .chatId(telegramUserId)
                .title(title)
                .description(description)
                .startParameter("get_access")
                .currency("RUB")
                .price(price)
                .needEmail(true)
                .needPhoneNumber(true)
                .protectContent(true)
                .build();
    }
}
