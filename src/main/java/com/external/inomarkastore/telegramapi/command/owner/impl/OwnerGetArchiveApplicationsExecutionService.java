package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static com.external.inomarkastore.constant.OwnerState.DELETE_APPLICATION;
import static com.external.inomarkastore.constant.OwnerState.GET_ARCHIVED_APPLICATIONS;
import static com.external.inomarkastore.constant.OwnerState.RESTORE_APPLICATION;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerGetArchiveApplicationsExecutionService implements OwnerActionExecuteService {
    private final ApplicationService applicationService;
    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;

    @Override
    public String getCommand() {
        return GET_ARCHIVED_APPLICATIONS.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var applications = applicationService.getArchivedApplications();
        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonObject.add("receivedArchivedApplicationsMessageId", new JsonPrimitive(messageIdFromUpdate));
        if (applications.isEmpty()) {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton("owner.youDoNotHaveArchiveApplications", user.getId(), jsonObject);
        } else {
            entitiesSendHelper.sendApplicationsMessageForOwner("owner.yourArchiveApplications", user.getId(), jsonObject);
            for (Application application : applications) {
                final var messageIds = new JsonArray();
                jsonObject.add(application.getId().toString(), messageIds);
                final var restoreApplicationButtonName =
                        messageSource.getMessage("buttonName.owner.restoreApplication");
                final var restoreApplicationCallbackData = "%s:%s".formatted(RESTORE_APPLICATION, application.getId());
                final var deleteApplicationButtonName = messageSource.getMessage("buttonName.owner.deleteApplication");
                final var deleteApplicationCallbackData = "%s:%s".formatted(DELETE_APPLICATION, application.getId());
                final var buttonNameToCallbackData = Map.of(restoreApplicationButtonName, restoreApplicationCallbackData,
                        deleteApplicationButtonName, deleteApplicationCallbackData);
                entitiesSendHelper.createAndSendApplicationMessage(user.getId(), application, messageIds, buttonNameToCallbackData);
            }
        }
        ownerInfoService.updateState(GET_ARCHIVED_APPLICATIONS);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
