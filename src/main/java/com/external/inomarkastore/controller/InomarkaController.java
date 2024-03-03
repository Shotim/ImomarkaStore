package com.external.inomarkastore.controller;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.controller.dto.ApplicationInputDto;
import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.model.CarDetails;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.external.inomarkastore.constant.ApplicationStatus.FULLY_CREATED;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getPhotoFromMessage;
import static com.external.inomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;
import static com.external.inomarkastore.util.ValidationUtils.formatAndValidateVinNumber;
import static io.micrometer.common.util.StringUtils.isBlank;
import static io.micrometer.common.util.StringUtils.isNotBlank;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequiredArgsConstructor
public class InomarkaController {

    private final CarDetailsService carDetailsService;
    private final ApplicationService applicationService;
    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;
    private final InomarkaStore inomarkaStore;

    private static void validateVinNumber(ApplicationInputDto applicationInput, MultipartFile vinNumberPhoto, String vinNumber, ArrayList<String> errors) {
        if (isBlank(vinNumber) == vinNumberPhoto.isEmpty()) {
            errors.add("VIN number should contain text or photo");
        }
        if (vinNumberPhoto.isEmpty() && isNotBlank(vinNumber)) {
            try {
                final var formattedVinNumber = formatAndValidateVinNumber(vinNumber);
                applicationInput.setVinNumber(formattedVinNumber);
            } catch (IllegalArgumentException exception) {
                errors.add(exception.getMessage());
            }
        }
    }

    private static void validateMainPurpose(MultipartFile mainPurposePhoto, String mainPurpose, ArrayList<String> errors) {
        if (isBlank(mainPurpose) == mainPurposePhoto.isEmpty()) {
            errors.add("Main purpose should contain text or photo");
        }
        if (mainPurposePhoto.isEmpty() && isNotBlank(mainPurpose) && mainPurpose.length() >= 255) {
            errors.add("Main purpose should be less than 255 characters");
        }
    }

    private static void validatePhoneNumber(ApplicationInputDto applicationInput, String phoneNumber, ArrayList<String> errors) {
        try {
            final var formattedPhoneNumber = formatAndValidatePhoneNumber(phoneNumber);
            applicationInput.setPhoneNumber(formattedPhoneNumber);
        } catch (IllegalArgumentException exception) {
            errors.add(exception.getMessage());
        }
    }

    private static void validateName(String name, ArrayList<String> errors) {
        if (isBlank(name) || name.length() >= 255) {
            errors.add("Name should be not empty and less than 255 characters");
        }
    }

    private static void validateComment(String comment, ArrayList<String> errors) {
        if (nonNull(comment) && comment.length() >= 255) {
            errors.add("Comment if exists should be less than 255 characters");
        }
    }

    private static void validateCarDetails(String carDetails, ArrayList<String> errors) {
        if (isBlank(carDetails) || carDetails.length() >= 255) {
            errors.add("Car details should be not empty and less than 255 characters");
        }
    }

    @SneakyThrows
    @Transactional
    @PostMapping("/application")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> createApplication(@RequestParam("text") String applicationInputDto,
                                                    @RequestParam("vinNumberPhoto") MultipartFile vinNumberPhoto,
                                                    @RequestParam("mainPurposePhoto") MultipartFile mainPurposePhoto) {
        final var applicationInput = new Gson().fromJson(applicationInputDto, ApplicationInputDto.class);
        validateInput(applicationInput, vinNumberPhoto, mainPurposePhoto);
        saveApplicationAndSendMessagesToOwner(applicationInput, vinNumberPhoto, mainPurposePhoto);
        return ResponseEntity.ok("Done");
    }

    private void validateInput(ApplicationInputDto applicationInput, MultipartFile vinNumberPhoto, MultipartFile mainPurposePhoto) {
        final var carDetails = applicationInput.getCarDetails();
        final var comment = applicationInput.getComment();
        final var name = applicationInput.getName();
        final var mainPurpose = applicationInput.getMainPurpose();
        final var vinNumber = applicationInput.getVinNumber();
        final var phoneNumber = applicationInput.getPhoneNumber();
        final var errors = new ArrayList<String>();
        validateCarDetails(carDetails, errors);
        validateComment(comment, errors);
        validateName(name, errors);
        validatePhoneNumber(applicationInput, phoneNumber, errors);
        validateMainPurpose(mainPurposePhoto, mainPurpose, errors);
        validateVinNumber(applicationInput, vinNumberPhoto, vinNumber, errors);
        if (!errors.isEmpty()) {
            final var errorMessage = errors.stream()
                    .collect(joining("; ", "Errors: ", EMPTY));
            throw new ResponseStatusException(BAD_REQUEST, errorMessage);
        }
    }

    private void saveApplicationAndSendMessagesToOwner(@Valid ApplicationInputDto applicationInputDto, MultipartFile vinNumberPhoto, MultipartFile mainPurposePhoto) throws TelegramApiException, IOException {

        final var vinNumber = applicationInputDto.getVinNumber();
        final var mainPurpose = applicationInputDto.getMainPurpose();
        final var phoneNumber = applicationInputDto.getPhoneNumber();
        final var inputCarDetails = applicationInputDto.getCarDetails();
        final var clientName = applicationInputDto.getName();
        final var comment = applicationInputDto.getComment();

        if (clientInfoService.getByPhoneNumberOpt(phoneNumber).isEmpty()) {
            clientInfoService.create(clientName, phoneNumber);
        }
        final var onPhoto = messageSource.getMessage("onPhoto");

        final var carDetails = carDetailsService.create();
        carDetails.setDetails(inputCarDetails);
        carDetails.setVinNumber(vinNumberPhoto.isEmpty() ? vinNumber : onPhoto);
        carDetails.setPhoneNumber(phoneNumber);
        final var updatedCarDetails = carDetailsService.update(carDetails);

        final var noComment = messageSource.getMessage("noComment");
        final var application = applicationService.create(phoneNumber);
        application.setCarDetailsId(updatedCarDetails.getId());
        application.setComment(isBlank(comment) ? noComment : comment);
        application.setMainPurpose(mainPurposePhoto.isEmpty() ? mainPurpose : onPhoto);
        final var updatedApplication = applicationService.update(application);


        final var photos = new ArrayList<InputStream>();
        for (MultipartFile multipartFile : List.of(vinNumberPhoto, mainPurposePhoto)) {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                InputStream inputStream = multipartFile.getInputStream();
                photos.add(inputStream);
            }
        }

        final var ownerUserId = ownerInfoService.getTelegramUserId();
        final var newApplicationCreatedText = messageSource.getMessage("owner.createdNewApplication");
        final var textMessageForOwner = createTextMessageForUser(ownerUserId, newApplicationCreatedText);
        final var sendMessageForOwnerMessageId = inomarkaStore.execute(textMessageForOwner).getMessageId();
        final var messageIds = new JsonArray();
        messageIds.add(sendMessageForOwnerMessageId);
        final var callbackData = "NEW_APPLICATION:%s".formatted(application.getId());
        final var skipApplicationButtonName = messageSource.getMessage("buttonName.owner.skipApplication");
        final var buttonNameToCallbackData = Map.of(
                skipApplicationButtonName,
                callbackData
        );
        final var messageList = entitiesSendHelper.createAndSendApplicationMessage(ownerUserId, updatedApplication, messageIds, buttonNameToCallbackData, photos);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.add(callbackData, messageIds);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        if (messageList.size() == 1) {
            final var photoSizeOptional = getPhotoFromMessage(messageList.get(0));
            if (!vinNumberPhoto.isEmpty()) {
                photoSizeOptional.ifPresent(photoSize -> updateCarDetails(carDetails, photoSize));
            }
            if (!mainPurposePhoto.isEmpty()) {
                photoSizeOptional.ifPresent(photoSize -> updateApplication(application, photoSize));
            }
        }
        if (messageList.size() == 2) {
            final var photoSizeVinNumberOptional = getPhotoFromMessage(messageList.get(0));
            photoSizeVinNumberOptional.ifPresent(photoSize -> updateCarDetails(carDetails, photoSize));
            final var photoSizeMainPurposeOptional = getPhotoFromMessage(messageList.get(1));
            photoSizeMainPurposeOptional.ifPresent(photoSize -> updateApplication(application, photoSize));
        }
        application.setStatus(FULLY_CREATED);
        applicationService.update(application);
    }

    private void updateApplication(Application application, PhotoSize photoSize) {
        application.setMainPurposePhotoId(photoSize.getFileId());
        applicationService.update(application);
    }

    private void updateCarDetails(CarDetails carDetails, PhotoSize photoSize) {
        carDetails.setVinNumberPhotoId(photoSize.getFileId());
        carDetailsService.update(carDetails);
    }
}
