package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.constant.OwnerState;
import com.external.imomarkastore.model.OwnerInfo;
import com.external.imomarkastore.repository.OwnerInfoRepository;
import com.external.imomarkastore.service.OwnerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerInfoServiceImpl implements OwnerInfoService {

    private final OwnerInfoRepository repository;

    @Override
    public void updateState(OwnerState state) {
        getOwnerInfoOptional()
                .ifPresentOrElse(
                        ownerInfo -> {
                            ownerInfo.setOwnerState(state);
                            repository.save(ownerInfo);
                        }, () -> {
                            throw new InternalError();
                        });
    }

    @Override
    public boolean isOwner(Long telegramUserId) {
        return repository.findById(telegramUserId).isPresent();
    }

    @Override
    public OwnerState getCurrentOwnerState() {
        return getOwnerInfoOptional()
                .map(OwnerInfo::getOwnerState)
                .orElseThrow(InternalError::new);
    }

    private Optional<OwnerInfo> getOwnerInfoOptional() {
        return repository.findAll().stream()
                .findFirst();
    }
}
