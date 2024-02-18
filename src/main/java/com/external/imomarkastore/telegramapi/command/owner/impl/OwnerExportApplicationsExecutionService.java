package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.external.imomarkastore.constant.OwnerState.EXPORT_APPLICATIONS;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.WHITE;
import static org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_JPEG;

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
    @SneakyThrows
    public void execute(Update update) {
        final var currentOwnerState = ownerInfoService.getCurrentOwnerState();
        final List<Application> applications = switch (currentOwnerState) {
            case GET_APPLICATIONS -> applicationService.getFullyCreatedApplications();
            case GET_ARCHIVED_APPLICATIONS -> applicationService.getArchivedApplications();
            default -> emptyList();
        };
        final var user = getUserFromUpdate(update);
        if (applications.isEmpty()) {
            final var text = messageSource.getMessage("owner.noApplicationsToExport");
            final var message = createTextMessageForUser(user, text);
            inomarkaStore.execute(message);
        } else {
            final var text = messageSource.getMessage("owner.exportedApplications");
            final var message = createTextMessageForUser(user, text);
            inomarkaStore.execute(message);
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
                    final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
                    final var clientInfoOptional = clientInfoService.getByTelegramUserId(application.getTelegramUserId());
                    final var row = sheet.createRow(rowNumber++);
                    final var cell0 = row.createCell(0);
                    cell0.setCellValue(application.getId());
                    cell0.setCellStyle(dataStyle);
                    final var carDetails = carDetailsOptional.map(CarDetails::getDetails).orElse(EMPTY);
                    final var cell1 = row.createCell(1);
                    cell1.setCellValue(carDetails);
                    cell1.setCellStyle(dataStyle);
                    final var vinNumber = carDetailsOptional.map(CarDetails::getVinNumber).orElse(EMPTY);
                    final var cell2 = row.createCell(2);
                    cell2.setCellValue(vinNumber);
                    cell2.setCellStyle(dataStyle);
                    final var vinNumberPhotoIdOptional = carDetailsOptional.map(CarDetails::getVinNumberPhotoId);
                    setPicture(vinNumberPhotoIdOptional, workbook, sheet, 3, rowNumber);
                    final var cell4 = row.createCell(4);
                    cell4.setCellValue(application.getMainPurpose());
                    cell4.setCellStyle(dataStyle);
                    final var mainPurposePhotoIdOptional = Optional.ofNullable(application.getMainPurposePhotoId());
                    setPicture(mainPurposePhotoIdOptional, workbook, sheet, 5, rowNumber);
                    final var cell6 = row.createCell(6);
                    cell6.setCellValue(application.getComment());
                    cell6.setCellStyle(dataStyle);
                    final var cell7 = row.createCell(7);
                    cell7.setCellValue(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(application.getCreatedAt()));
                    cell7.setCellStyle(dataStyle);
                    final var clientName = clientInfoOptional.map(ClientInfo::getName).orElse(EMPTY);
                    final var cell8 = row.createCell(8);
                    cell8.setCellValue(clientName);
                    cell8.setCellStyle(dataStyle);
                    final var clientPhoneNumber = clientInfoOptional.map(ClientInfo::getPhoneNumber).orElse(EMPTY);
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
                        .chatId(user.getId().toString())
                        .document(new InputFile(file, filePath))
                        .build();
                inomarkaStore.execute(sendDocument);
                file.delete();
            }
        }
    }

    private void setPicture(Optional<String> photoIdOptional, XSSFWorkbook workbook, XSSFSheet sheet, int col1, int rowNumber) throws TelegramApiException, IOException {
        if (photoIdOptional.isPresent()) {
            final var getFile = GetFile.builder()
                    .fileId(photoIdOptional.get())
                    .build();
            final var file = inomarkaStore.execute(getFile);
            final var photo = inomarkaStore.downloadFile(file);
            final var bytes = readFileToByteArray(photo);
            final var pictureIdx = workbook.addPicture(bytes, PICTURE_TYPE_JPEG);
            final var helper = workbook.getCreationHelper();
            final var drawing = sheet.createDrawingPatriarch();
            final var anchor = helper.createClientAnchor();
            anchor.setCol1(col1);
            anchor.setRow1(rowNumber - 1);
            final var picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize(0.1);
        }
    }
}
