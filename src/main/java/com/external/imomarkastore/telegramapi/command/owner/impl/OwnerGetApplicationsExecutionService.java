package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.imomarkastore.constant.OwnerState.PREPARE_PAYMENT;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class OwnerGetApplicationsExecutionService implements OwnerActionExecuteService {
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
                final var archiveApplicationCallbackData = "%s:%s".formatted(MOVE_APPLICATION_TO_ARCHIVE, application.getId());
                final var sendPaymentButtonName = messageSource.getMessage("buttonName.owner.sendPayment");
                final var sendPaymentCallbackData = "%s:%s".formatted(PREPARE_PAYMENT, application.getId());

                final var buttonNameToCallbackData = new HashMap<String, String>();
                buttonNameToCallbackData.put(archiveApplicationButtonName, archiveApplicationCallbackData);
                if (!application.isSentRequestForPayment() && nonNull(application.getTelegramUserId())) {
                    buttonNameToCallbackData.put(sendPaymentButtonName, sendPaymentCallbackData);
                }
                entitiesSendHelper.createAndSendApplicationMessage(user.getId(), application, messageIds, buttonNameToCallbackData);
            }
        }
        ownerInfoService.updateState(GET_APPLICATIONS);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
