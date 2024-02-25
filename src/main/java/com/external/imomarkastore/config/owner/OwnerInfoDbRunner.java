package com.external.imomarkastore.config.owner;

import com.external.imomarkastore.model.OwnerInfo;
import com.external.imomarkastore.repository.OwnerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static com.external.imomarkastore.constant.OwnerState.START;

@Component
@RequiredArgsConstructor
public class OwnerInfoDbRunner implements ApplicationRunner {

    private final OwnerInfoRepository repository;
    @Value("${bot.owner.telegram_id}")
    private final Long ownerTelegramUserId;

    @Override
    public void run(ApplicationArguments args) {
        final var empty = repository.findAll().isEmpty();
        if (empty) {
            final var ownerInfo = new OwnerInfo();
            ownerInfo.setTelegramUserId(ownerTelegramUserId);
            ownerInfo.setOwnerState(START);
            ownerInfo.setAddress("г. Костомукша, ул. Ленина 12, вход с торца");
            ownerInfo.setEmail("10avtodom@gmail.com");
            ownerInfo.setInn("100400090604");
            ownerInfo.setOwnerName("Лазарчук Дмитрий Павлович");
            ownerInfo.setPhoneNumber("+79114371484");
            ownerInfo.setWorkingHours("Будние: 10.00-18.00");
            repository.save(ownerInfo);
        }
    }
}
