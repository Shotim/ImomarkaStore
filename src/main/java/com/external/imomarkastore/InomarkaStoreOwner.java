package com.external.imomarkastore;

import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.exception.BotException;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Component
@Slf4j
public class InomarkaStoreOwner {
    private final Map<String, OwnerState> commandToOwnerStateMatrix;
    private Map<String, OwnerActionExecuteService> ownerActionExecutionServicesByStateName;
    private final Map<String, OwnerState> buttonToOwnerStateMatrix;
    private final Map<OwnerState, List<OwnerState>> ownerStateMatrix;
    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;

    public InomarkaStoreOwner(
            BotMessageSource messageSource,
            OwnerInfoService ownerInfoService,
            @Qualifier("commandToOwnerStateMatrix") Map<String, OwnerState> commandToOwnerStateMatrix,
            @Qualifier("buttonToOwnerStateMatrix") Map<String, OwnerState> buttonToOwnerStateMatrix,
            @Qualifier("ownerStateMatrix") Map<OwnerState, List<OwnerState>> ownerStateMatrix) {
        this.messageSource = messageSource;
        this.ownerInfoService = ownerInfoService;
        this.commandToOwnerStateMatrix = commandToOwnerStateMatrix;
        this.buttonToOwnerStateMatrix = buttonToOwnerStateMatrix;
        this.ownerStateMatrix = ownerStateMatrix;
    }

    @Autowired
    @Lazy
    public void setOwnerActionExecutionServicesByStateName(
            @Qualifier("ownerActionExecutionServicesByStateName")
            Map<String, OwnerActionExecuteService> ownerActionExecutionServicesByStateName) {
        this.ownerActionExecutionServicesByStateName = ownerActionExecutionServicesByStateName;
    }

    public void processAction(Update update) {
        final var text = getTextFromUpdate(update);
        final var ownerState = commandToOwnerStateMatrix.get(text);
        if (nonNull(ownerState)) {
            processOwnerCommand(update, ownerState);
        } else {
            final var currentOwnerState = ownerInfoService.getCurrentOwnerState();
            final var nextStates = ownerStateMatrix.get(currentOwnerState);
            final var buttonState = isNotBlank(text) ? buttonToOwnerStateMatrix.get(text) : null;
            if (nonNull(buttonState) && !nextStates.contains(buttonState)) {
                throwException(update, nextStates);
            }
            final var ownerStateFromUpdate = isNotEmpty(nextStates) && nextStates.size() == 1 ?
                    nextStates.get(0) : buttonState;
            final var ownerActionExecuteService = ownerActionExecutionServicesByStateName.get(ownerStateFromUpdate.name());
            if (nonNull(ownerActionExecuteService)) {
                ownerActionExecuteService.execute(update);
            } else {
                final var errorText = messageSource.getMessage("error.botConfig");
                final var user = getUserFromUpdate(update);
                throwException(user, errorText);
            }
        }

    }

    private void processOwnerCommand(Update update, OwnerState ownerState) {
        final var ownerActionExecuteService = ownerActionExecutionServicesByStateName.get(ownerState.name());
        if (nonNull(ownerActionExecuteService)) {
            ownerActionExecuteService.execute(update);
        } else {
            final var errorText = messageSource.getMessage("error.botConfig");
            final var user = getUserFromUpdate(update);
            throwException(user, errorText);
        }
    }

    private void throwException(User user, String errorText) {
        final var errorMessage = createTextMessageForUser(user, errorText);
        throw new BotException(errorMessage);
    }

    private void throwException(Update update, List<OwnerState> nextStates) {
        log.error("Wrong command");
        final var user = getUserFromUpdate(update);
        final var availableStates = nextStates.stream()
                .filter(Objects::nonNull)
                .map(OwnerState::getOwnerStateText)
                .collect(joining(", "));
        final var errorText = messageSource.getMessage("error.nextStates", List.of(availableStates).toArray());
        throwException(user, errorText);
    }
}
