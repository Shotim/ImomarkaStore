package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.ClientState.PAY_ORDER;
import static com.external.inomarkastore.constant.OwnerState.SEND_PAYMENT;
import static com.external.inomarkastore.util.MessageUtils.createSendInvoiceForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class OwnerSendPaymentExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return SEND_PAYMENT.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var text = getTextFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var applicationIdToPayOptional = jsonDataObject.keySet().stream()
                .filter(key -> key.startsWith("applicationIdToPay"))
                .findFirst();

        if (applicationIdToPayOptional.isPresent()) {
            final var applicationIdString = applicationIdToPayOptional.get();
            final var applicationId = Long.valueOf(applicationIdString.substring(applicationIdString.indexOf(":") + 1));
            final var application = applicationService.getById(applicationId);
            final var telegramUserId = application.getTelegramUserId();
            if (nonNull(telegramUserId)) {
                final var clientInfo = clientInfoService.getByTelegramUserId(application.getTelegramUserId());
                jsonDataObject.addProperty("receivedSendPaymentMessageId", messageIdFromUpdate);
                application.setSentRequestForPayment(true);
                final var prevClientState = clientInfo.getState();
                final var additionalJsonDataForNextOperations = clientInfo.getAdditionalJsonDataForNextOperations();
                final var jsonObject = isBlank(additionalJsonDataForNextOperations) ?
                        new JsonObject() :
                        new Gson().fromJson(additionalJsonDataForNextOperations, JsonObject.class);
                jsonObject.addProperty("prevState", prevClientState.name());
                clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
                clientInfo.setState(PAY_ORDER);
                sendInvoiveMessage(text, applicationId, telegramUserId);
                sendPaymentSentMessage(user, jsonDataObject);
                clientInfoService.update(clientInfo);
                applicationService.update(application);
                ownerInfoService.updateState(SEND_PAYMENT);
                ownerInfoService.updateJsonData(jsonDataObject.toString());
            }
        }
    }

    private void sendInvoiveMessage(String text, Long applicationId, Long telegramUserId) throws TelegramApiException {
        final var title = messageSource.getMessage("owner.paymentTitle");
        final var description = messageSource.getMessage("owner.paymentDescription", List.of(applicationId).toArray());
        final var priceAsDouble = Double.parseDouble(text);
        final var priceValue = Double.valueOf(priceAsDouble * 100).intValue();
        final var sendInvoice = createSendInvoiceForUser(telegramUserId, title, description, priceValue, applicationId.toString());
        inomarkaStore.execute(sendInvoice);
    }

    private void sendPaymentSentMessage(User user, JsonObject jsonDataObject) throws TelegramApiException {
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
