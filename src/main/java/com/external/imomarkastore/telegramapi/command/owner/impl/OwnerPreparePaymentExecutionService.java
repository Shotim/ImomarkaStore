package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.PREPARE_PAYMENT;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getLongIdFromCallbackData;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerPreparePaymentExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final DeleteMessagesHelper deleteMessagesHelper;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return PREPARE_PAYMENT.name();
    }

    @Override
    @Transactional
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var applicationId = getLongIdFromCallbackData(update);
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var applicationOptional = applicationService.getById(applicationId);
        if (applicationOptional.isPresent()) {
            final var callbackText = messageSource.getMessage("owner.callback.preparePayment");
            final var answerCallbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, callbackText);
            inomarkaStore.execute(answerCallbackQuery);
            final var application = applicationOptional.get();
            final var jsonDataObject = ownerInfoService.getJsonDataObject();
            deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user, jsonDataObject);
            final var applicationPayloadForOwner = applicationService.getApplicationPayloadForOwner(application);
            final var enterPaymentSumText = messageSource.getMessage("owner.enterPaymentSum");
            final var text = applicationPayloadForOwner + "\n\n" + enterPaymentSumText;
            final var buttonNames = List.of(
                    messageSource.getMessage("buttonName.owner.backToApplications")
            );
            final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user, text, buttonNames);
            final var sendMessageId = inomarkaStore.execute(sendMessage).getMessageId();
            final var jsonObject = new JsonObject();
            jsonObject.addProperty("applicationIdToPay:%s".formatted(application.getId()), sendMessageId);
            ownerInfoService.updateJsonData(jsonObject.toString());
            ownerInfoService.updateState(PREPARE_PAYMENT);
        }
    }
}
