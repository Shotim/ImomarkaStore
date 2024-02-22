package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.ApplicationStatus.ARCHIVED;
import static com.external.imomarkastore.constant.ClientState.PAY_ORDER;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderExecutionService implements MessageExecutionService {

    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return PAY_ORDER;
    }

    @Override
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        if (update.hasPreCheckoutQuery()) {
            final var preCheckoutQuery = update.getPreCheckoutQuery();
            final var invoicePayload = preCheckoutQuery.getInvoicePayload();
            final var applicationId = Long.valueOf(invoicePayload);
            final var applicationOptional = applicationService.getById(applicationId);
            if (applicationOptional.isPresent()) {
                answerPreCheckoutQuery(preCheckoutQuery);
            }
        } else if (update.getMessage().hasSuccessfulPayment()) {
            final var successfulPayment = update.getMessage().getSuccessfulPayment();
            final var invoicePayload = successfulPayment.getInvoicePayload();
            final var applicationId = Long.valueOf(invoicePayload);
            final var applicationOptional = applicationService.getById(applicationId);
            if (applicationOptional.isPresent()) {
                updateClientInfoWithPrevState(clientInfo);
                moveApplicationToArchive(applicationOptional.get());
                sendMessageToOwner(clientInfo, applicationId);
            }
        } else {
            final var user = getUserFromUpdate(update);
            final var text = messageSource.getMessage("haveToPay");
            final var textMessageForUser = createTextMessageForUser(user.getId(), text);
            inomarkaStore.execute(textMessageForUser);
        }
    }

    private void updateClientInfoWithPrevState(ClientInfo clientInfo) {
        final var additionalJsonDataForNextOperations = clientInfo.getAdditionalJsonDataForNextOperations();
        final var jsonObject = new Gson().fromJson(additionalJsonDataForNextOperations, JsonObject.class);
        final var prevStateString = jsonObject.remove("prevState").getAsString();
        final var clientState = ClientState.valueOf(prevStateString);
        clientInfo.setState(clientState);
        clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
        clientInfoService.update(clientInfo);
    }

    private void answerPreCheckoutQuery(PreCheckoutQuery preCheckoutQuery) throws TelegramApiException {
        final var answerPreCheckoutQuery = AnswerPreCheckoutQuery.builder()
                .ok(true)
                .preCheckoutQueryId(preCheckoutQuery.getId())
                .build();
        inomarkaStore.execute(answerPreCheckoutQuery);
    }

    private void sendMessageToOwner(ClientInfo clientInfo, Long applicationId) throws TelegramApiException {
        final var clientName = clientInfo.getName();
        final var clientPhoneNumber = clientInfo.getPhoneNumber();
        final var messageTextForOwner = messageSource.getMessage("owner.paymentReceived",
                List.of(clientName, clientPhoneNumber, applicationId).toArray());
        final var ownerTelegramUserId = ownerInfoService.getTelegramUserId();
        final var textMessageForOwner = createTextMessageForUser(ownerTelegramUserId, messageTextForOwner);
        inomarkaStore.execute(textMessageForOwner);
    }

    private void moveApplicationToArchive(Application application) {
        application.setStatus(ARCHIVED);
        application.setPaid(true);
        applicationService.update(application);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) {
        log.info("Not used");
    }
}
