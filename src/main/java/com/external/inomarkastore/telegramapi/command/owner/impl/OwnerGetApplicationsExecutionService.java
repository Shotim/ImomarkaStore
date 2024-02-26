package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

import static com.external.inomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.inomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.inomarkastore.constant.OwnerState.PREPARE_PAYMENT;
import static com.external.inomarkastore.constant.OwnerState.SET_PAYMENT;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class OwnerGetApplicationsExecutionService implements OwnerActionExecuteService {
    private static final String CALLBACK_DATA_TEMPLATE = "%s:%s";
    private final ApplicationService applicationService;
    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return GET_APPLICATIONS.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var applications = applicationService.getFullyCreatedApplications();
        final var jsonObject = ownerInfoService.getJsonDataObject();
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonObject);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonObject.add("receivedApplicationsMessageId", new JsonPrimitive(messageIdFromUpdate));
        if (applications.isEmpty()) {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton("owner.youDoNotHaveActiveApplications", user.getId(), jsonObject);
        } else {
            entitiesSendHelper.sendApplicationsMessageForOwner("owner.yourActiveApplications", user.getId(), jsonObject);
            for (Application application : applications) {
                final var messageIds = new JsonArray();
                jsonObject.add(application.getId().toString(), messageIds);

                final var archiveApplicationButtonName =
                        messageSource.getMessage("buttonName.owner.archiveApplication");
                final var archiveApplicationCallbackData = CALLBACK_DATA_TEMPLATE.formatted(MOVE_APPLICATION_TO_ARCHIVE, application.getId());
                final var buttonNameToCallbackData = new HashMap<String, String>();
                buttonNameToCallbackData.put(archiveApplicationButtonName, archiveApplicationCallbackData);

                if (!application.isSentRequestForPayment() && nonNull(application.getTelegramUserId())) {
                    final var sendPaymentButtonName = messageSource.getMessage("buttonName.owner.sendPayment");
                    final var sendPaymentCallbackData = CALLBACK_DATA_TEMPLATE.formatted(PREPARE_PAYMENT, application.getId());
                    buttonNameToCallbackData.put(sendPaymentButtonName, sendPaymentCallbackData);
                }
                if (!application.isPaid()) {
                    final var setPaymentButtonName = messageSource.getMessage("buttonName.owner.setPayment");
                    final var setPaymentCallbackData = CALLBACK_DATA_TEMPLATE.formatted(SET_PAYMENT, application.getId());
                    buttonNameToCallbackData.put(setPaymentButtonName, setPaymentCallbackData);
                }
                entitiesSendHelper.createAndSendApplicationMessage(user.getId(), application, messageIds, buttonNameToCallbackData);
            }
        }
        ownerInfoService.updateState(GET_APPLICATIONS);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
