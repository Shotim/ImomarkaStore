package com.external.imomarkastore;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Slf4j
@Component
public class InomarkaStore extends TelegramLongPollingBot {

    private final String botName;
    private final Map<ClientState, List<ClientState>> clientStateMatrix;
    private final Map<String, ClientState> buttonClientToStateMatrix;
    private Map<String, CommandExecutionService> clientCommandExecutionServicesByCommands;
    private final List<ClientState> callbackExecutionClientStates;
    private final List<ClientState> messageExecutionClientStates;
    private Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState;
    private final Map<String, OwnerState> commandToOwnerStateMatrix;
    private Map<String, OwnerActionExecuteService> ownerActionExecutionServicesByStateName;
    private final ClientInfoService clientInfoService;
    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;

    public InomarkaStore(@Value("${bot.token}") String botToken,
                         ClientInfoService clientInfoService,
                         BotMessageSource messageSource,
                         OwnerInfoService ownerInfoService,
                         @Value("${bot.name}") String botName,
                         @Qualifier("clientStateMatrix") Map<ClientState, List<ClientState>> clientStateMatrix,
                         @Qualifier("buttonToClientStateMatrix") Map<String, ClientState> buttonClientToStateMatrix,
                         @Qualifier("callbackExecutionClientStates") List<ClientState> callbackExecutionClientStates,
                         @Qualifier("messageExecutionClientStates") List<ClientState> messageExecutionClientStates,
                         @Qualifier("commandToOwnerStateMatrix") Map<String, OwnerState> commandToOwnerStateMatrix) {
        super(botToken);
        this.botName = botName;
        this.clientStateMatrix = clientStateMatrix;
        this.clientInfoService = clientInfoService;
        this.buttonClientToStateMatrix = buttonClientToStateMatrix;
        this.callbackExecutionClientStates = callbackExecutionClientStates;
        this.messageExecutionClientStates = messageExecutionClientStates;
        this.messageSource = messageSource;
        this.ownerInfoService = ownerInfoService;
        this.commandToOwnerStateMatrix = commandToOwnerStateMatrix;
    }

    @Autowired
    @Lazy
    public void setClientCommandExecutionServicesByCommands(
            @Qualifier("clientCommandExecutionServicesByCommands")
            Map<String, CommandExecutionService> clientCommandExecutionServicesByCommands) {
        this.clientCommandExecutionServicesByCommands = clientCommandExecutionServicesByCommands;
    }

    @Autowired
    @Lazy
    public void setMessageExecutionServicesByClientState(
            @Qualifier("messageExecutionServicesByClientState")
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState) {
        this.messageExecutionServicesByClientState = messageExecutionServicesByClientState;
    }

    @Autowired
    @Lazy
    public void setOwnerActionExecutionServicesByStateName(
            @Qualifier("ownerActionExecutionServicesByStateName")
            Map<String, OwnerActionExecuteService> ownerActionExecutionServicesByStateName) {
        this.ownerActionExecutionServicesByStateName = ownerActionExecutionServicesByStateName;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final var user = getUserFromUpdate(update);
        if (ownerInfoService.isOwner(user.getId())) {
            processOwnerAction(update);
        } else {
            processClientAction(update, user);
        }
    }

    private void processOwnerAction(Update update) {
        final var text = getTextFromUpdate(update);
        final var ownerState = commandToOwnerStateMatrix.get(text);
        if (nonNull(ownerState)) {
            processOwnerCommand(update, ownerState);
        } else {
            // TODO
        }

    }

    private void processOwnerCommand(Update update, OwnerState ownerState) {
        final var ownerActionExecuteService = ownerActionExecutionServicesByStateName.get(ownerState.name());
        if (nonNull(ownerActionExecuteService)) {
            ownerActionExecuteService.execute(update);
        } else {
            final var errorText = messageSource.getMessage("error.botConfig");
            final var user = getUserFromUpdate(update);
            sendErrorMessage(user, errorText);
        }
    }

    private void processClientAction(Update update, User user) {
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(user.getId());
        if (update.hasCallbackQuery()) {
            processClientCallBacks(update, clientInfoOptional);
        } else {
            processClientMessagesAndCommands(update, clientInfoOptional);
        }
    }

    private void processClientMessagesAndCommands(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var text = getTextFromUpdate(update);
        final var commandExecutionService = clientCommandExecutionServicesByCommands.get(text);
        if (nonNull(commandExecutionService)) {
            commandExecutionService.execute(update);
        } else {
            processClientMessages(update, clientInfoOptional, text);
        }
    }

    private void processClientMessages(Update update, Optional<ClientInfo> clientInfoOptional, String text) {
        final var user = getUserFromUpdate(update);
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = clientStateMatrix.get(clientInfoState);
            final var messageNextStates = nextStates.stream()
                    .filter(messageExecutionClientStates::contains).toList();
            final var buttonState = isNotBlank(text) ? buttonClientToStateMatrix.get(text) : null;
            if (nonNull(buttonState) && !messageNextStates.contains(buttonState)) {
                sendErrorMessage(update, nextStates);
                return;
            }
            final var clientState = isNotEmpty(messageNextStates) && messageNextStates.size() == 1 ?
                    messageNextStates.get(0) :
                    buttonState;
            final var messageExecutionService = messageExecutionServicesByClientState.get(clientState);
            if (nonNull(messageExecutionService)) {
                messageExecutionService.execute(update, clientInfo);
            } else {
                final var errorText = messageSource.getMessage("error.botConfig");
                sendErrorMessage(user, errorText);
            }
        } else {
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            sendErrorMessage(user, errorText);
        }
    }

    private void processClientCallBacks(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var data = update.getCallbackQuery().getData();
        final var callbackState = data.substring(0, data.indexOf(":"));
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = clientStateMatrix.get(clientInfoState);
            final var callbackNextStates = nextStates.stream()
                    .filter(callbackExecutionClientStates::contains)
                    .toList();
            final var nextState = ClientState.valueOf(callbackState);
            if (!callbackNextStates.contains(nextState)) {
                sendErrorMessage(update, nextStates);
            } else {
                final var messageExecutionService = messageExecutionServicesByClientState.get(nextState);
                messageExecutionService.execute(update, clientInfo);
            }
        } else {
            final var user = getUserFromUpdate(update);
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            sendErrorMessage(user, errorText);
        }
    }

    @SneakyThrows
    private void sendErrorMessage(User user, String errorText) {
        final var errorMessage = createTextMessageForUser(user, errorText);
        this.execute(errorMessage);
    }


    private void sendErrorMessage(Update update, List<ClientState> nextStates) {
        log.error("Wrong command");
        final var user = getUserFromUpdate(update);
        final var availableStates = nextStates.stream()
                .filter(Objects::nonNull)
                .map(ClientState::getClientStateText)
                .collect(joining(", "));
        final var errorText = messageSource.getMessage("error.nextStates", List.of(availableStates).toArray());
        sendErrorMessage(user, errorText);
    }
}
