package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.CONFIRM_PAYMENT;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerConfirmPaymentExecutionService implements OwnerActionExecuteService {

    public static final String RECEIVED_SET_PAYMENT_MESSAGE_IDS = "receivedSetPaymentMessageIds";
    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return CONFIRM_PAYMENT.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var text = getTextFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var applicationIdToPayOptional = jsonDataObject.keySet().stream()
                .filter(key -> key.startsWith("applicationIdToSetPayment"))
                .findFirst();
        if (applicationIdToPayOptional.isPresent()) {
            final var applicationIdString = applicationIdToPayOptional.get();
            final var applicationId = Long.valueOf(applicationIdString.substring(applicationIdString.indexOf(":") + 1));
            final var applicationOptional = applicationService.getById(applicationId);
            if (applicationOptional.isPresent()) {
                final var application = applicationOptional.get();
                setMessageId(messageIdFromUpdate, jsonDataObject);
                if ("Оплата".equalsIgnoreCase(text)) {
                    sendPaymentSetMessage(user, jsonDataObject);
                    application.setPaid(true);
                    application.setSentRequestForPayment(true);
                    applicationService.update(application);
                    ownerInfoService.updateState(CONFIRM_PAYMENT);
                    ownerInfoService.updateJsonData(jsonDataObject.toString());
                } else {
                    final var errorText = messageSource.getMessage("error.wrongConfirm");
                    final var errorMessage = createTextMessageForUser(user.getId(), errorText);
                    final var errorMessageId = inomarkaStore.execute(errorMessage).getMessageId();
                    setMessageId(errorMessageId, jsonDataObject);
                }
            }
        }
    }

    private void setMessageId(Integer messageIdFromUpdate, JsonObject jsonDataObject) {
        if (jsonDataObject.has(RECEIVED_SET_PAYMENT_MESSAGE_IDS)) {
            final var receivedSetPaymentMessageIds = jsonDataObject.get(RECEIVED_SET_PAYMENT_MESSAGE_IDS).getAsJsonArray();
            receivedSetPaymentMessageIds.add(messageIdFromUpdate);
        } else {
            final var messageIds = new JsonArray();
            messageIds.add(messageIdFromUpdate);
            jsonDataObject.add(RECEIVED_SET_PAYMENT_MESSAGE_IDS, messageIds);
        }
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }

    private void sendPaymentSetMessage(User user, JsonObject jsonDataObject) throws TelegramApiException {
        final var paymentSentMessage = messageSource.getMessage("owner.paymentSent");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToApplications"),
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), paymentSentMessage, buttonNames);
        final var sentMessageId = inomarkaStore.execute(sendMessage).getMessageId();
        jsonDataObject.addProperty("sentSendPaymentMessageId", sentMessageId);
    }
}
