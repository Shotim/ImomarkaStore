package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerGetApplicationsExecutionService implements OwnerActionExecuteService {
    private final ApplicationService applicationService;
    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;

    @Override
    public String getCommand() {
        return GET_APPLICATIONS.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var applications = applicationService.getFullyCreatedApplications();
        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonObject.add("receivedApplicationsMessageId", new JsonPrimitive(messageIdFromUpdate));
        if (applications.isEmpty()) {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton("owner.youDoNotHaveActiveApplications", user, jsonObject);
        } else {
            entitiesSendHelper.sendApplicationsMessageForOwner("owner.yourActiveApplications", user, jsonObject);
            for (Application application : applications) {
                final var messageIds = new JsonArray();
                jsonObject.add(application.getId().toString(), messageIds);
                final var archiveApplicationButtonName =
                        messageSource.getMessage("buttonName.owner.archiveApplication");
                final var callbackData = "%s:%s".formatted(MOVE_APPLICATION_TO_ARCHIVE, application.getId());
                entitiesSendHelper.createAndSendApplicationMessage(user, application, messageIds, archiveApplicationButtonName, callbackData);
            }
        }
        ownerInfoService.updateState(GET_APPLICATIONS);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
