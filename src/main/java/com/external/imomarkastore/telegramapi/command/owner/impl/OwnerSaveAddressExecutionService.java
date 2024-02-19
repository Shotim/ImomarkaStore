package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.SAVE_ADDRESS;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerSaveAddressExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return SAVE_ADDRESS.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var text = getTextFromUpdate(update);
        ownerInfoService.updateAddress(text);
        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editAddress.success");
        final var textMessageForUser = createTextMessageForUser(user, message);
        final var successfulAddressSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonDataObject.add("receivedSaveAddressMessageId", new JsonPrimitive(messageIdFromUpdate));
        jsonDataObject.add("successfulAddressSaveMessageId", new JsonPrimitive(successfulAddressSaveMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(GET_CONTACTS);
        deleteMessagesHelper.deleteMessagesFromJsonDataForUser(user, jsonDataObject);
        ownerInfoService.updateJsonData(null);
        ownerGetContactsExecutionService.execute(update);
    }
}
