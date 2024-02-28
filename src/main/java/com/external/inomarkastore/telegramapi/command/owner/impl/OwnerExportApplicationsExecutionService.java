package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.external.inomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.nio.file.Files.delete;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.WHITE;

@Service
@RequiredArgsConstructor
public class OwnerExportApplicationsExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;
    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return EXPORT_APPLICATIONS.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var currentOwnerState = ownerInfoService.getCurrentOwnerState();

        final List<Application> applications = switch (currentOwnerState) {
            case GET_APPLICATIONS -> applicationService.getFullyCreatedApplications();
            case GET_ARCHIVED_APPLICATIONS -> applicationService.getArchivedApplications();
            default -> emptyList();
        };
        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonObject.add("receivedExportApplicationsMessageId", new JsonPrimitive(messageIdFromUpdate));
        final var messageIds = new JsonArray();
        jsonObject.add("exportMessageIds", messageIds);
        final var user = getUserFromUpdate(update);
        if (applications.isEmpty()) {
            final var text = messageSource.getMessage("owner.noApplicationsToExport");
            final var message = createTextMessageForUser(user.getId(), text);
            final var messageId = inomarkaStore.execute(message).getMessageId();
            messageIds.add(messageId);
        } else {
            final var text = messageSource.getMessage("owner.exportedApplications");
            final var message = createTextMessageForUser(user.getId(), text);
            final var messageId = inomarkaStore.execute(message).getMessageId();
            messageIds.add(messageId);
            try (final var workbook = new XSSFWorkbook()) {
                final var sheetName = messageSource.getMessage("owner.excel.sheet");
                final var sheet = workbook.createSheet(sheetName);
                final var headerRow = sheet.createRow(0);
                headerRow.setHeight((short) -1);
                final var columnNames = new String[]{
                        messageSource.getMessage("owner.excel.field.0"),
                        messageSource.getMessage("owner.excel.field.1"),
                        messageSource.getMessage("owner.excel.field.2"),
                        messageSource.getMessage("owner.excel.field.3"),
                        messageSource.getMessage("owner.excel.field.4"),
                        messageSource.getMessage("owner.excel.field.5"),
                        messageSource.getMessage("owner.excel.field.6"),
                        messageSource.getMessage("owner.excel.field.7"),
                        messageSource.getMessage("owner.excel.field.8"),
                        messageSource.getMessage("owner.excel.field.9")
                };
                final var headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(SOLID_FOREGROUND);

                final var font = workbook.createFont();
                font.setFontName("Arial");
                font.setFontHeightInPoints((short) 20);
                font.setBold(true);
                headerStyle.setFont(font);

                final var dataStyle = workbook.createCellStyle();
                dataStyle.setFillForegroundColor(WHITE.getIndex());
                dataStyle.setFillPattern(SOLID_FOREGROUND);

                final var dataFont = workbook.createFont();
                dataFont.setFontName("Arial");
                dataFont.setFontHeightInPoints((short) 20);
                dataStyle.setFont(dataFont);

                for (int counter = 0; counter < columnNames.length; counter++) {
                    final var cell = headerRow.createCell(counter);
                    cell.setCellValue(columnNames[counter]);
                    cell.setCellStyle(dataStyle);
                }
                var rowNumber = 1;
                for (Application application : applications) {
                    final var carDetails = carDetailsService.getById(application.getCarDetailsId());
                    final var clientInfo = clientInfoService.getByTelegramUserIdOrPhoneNumber(application.getTelegramUserId(), application.getPhoneNumber());
                    final var row = sheet.createRow(rowNumber++);
                    final var cell0 = row.createCell(0);
                    cell0.setCellValue(application.getId());
                    cell0.setCellStyle(dataStyle);
                    final var details = Optional.ofNullable(carDetails.getDetails()).orElse(EMPTY);
                    final var cell1 = row.createCell(1);
                    cell1.setCellValue(details);
                    cell1.setCellStyle(dataStyle);
                    final var vinNumber = Optional.ofNullable(carDetails.getVinNumber()).orElse(EMPTY);
                    final var cell2 = row.createCell(2);
                    cell2.setCellValue(vinNumber);
                    cell2.setCellStyle(dataStyle);
                    final var vinNumberPhotoId = Optional.ofNullable(carDetails.getVinNumberPhotoId()).orElse(EMPTY);
                    final var cell3 = row.createCell(3);
                    cell3.setCellValue(vinNumberPhotoId);
                    cell3.setCellStyle(dataStyle);
                    final var cell4 = row.createCell(4);
                    cell4.setCellValue(application.getMainPurpose());
                    cell4.setCellStyle(dataStyle);
                    final var mainPurposePhotoId = Optional.ofNullable(application.getMainPurposePhotoId()).orElse(EMPTY);
                    final var cell5 = row.createCell(5);
                    cell5.setCellValue(mainPurposePhotoId);
                    cell5.setCellStyle(dataStyle);
                    final var cell6 = row.createCell(6);
                    cell6.setCellValue(application.getComment());
                    cell6.setCellStyle(dataStyle);
                    final var cell7 = row.createCell(7);
                    cell7.setCellValue(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(application.getCreatedAt()));
                    cell7.setCellStyle(dataStyle);
                    final var clientName = Optional.ofNullable(clientInfo.getName()).orElse(EMPTY);
                    final var cell8 = row.createCell(8);
                    cell8.setCellValue(clientName);
                    cell8.setCellStyle(dataStyle);
                    final var clientPhoneNumber = Optional.ofNullable(clientInfo.getPhoneNumber()).orElse(EMPTY);
                    final var cell9 = row.createCell(9);
                    cell9.setCellValue(clientPhoneNumber);
                    cell9.setCellStyle(dataStyle);
                    row.setHeight((short) -1);
                }
                for (int counter = 0; counter < columnNames.length; counter++) {
                    sheet.autoSizeColumn(counter);
                }
                String filePath = "result.xlsx";
                try (final var fileOutputStream = new FileOutputStream(filePath)) {
                    workbook.write(fileOutputStream);
                }
                final var file = getFile(filePath);
                final var sendDocument = SendDocument.builder()
                        .chatId(user.getId())
                        .document(new InputFile(file, filePath))
                        .build();
                final var exportFileMessageId = inomarkaStore.execute(sendDocument).getMessageId();
                messageIds.add(exportFileMessageId);
                delete(file.toPath());
            } catch (IOException e) {
                throw new BusinessLogicException("An error occured while creating Excel document: " + e.getMessage());
            }
        }
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
