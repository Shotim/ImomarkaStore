package com.external.imomarkastore.telegramapi.command.util;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EntitiesSendHelperUtils {

    public static List<String> getPhotoIds(Application application, Optional<CarDetails> carDetailsOptional) {
        return Stream.of(
                        Optional.ofNullable(application.getMainPurposePhotoId()),
                        carDetailsOptional.map(CarDetails::getVinNumberPhotoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
