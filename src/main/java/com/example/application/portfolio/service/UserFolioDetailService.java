package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.models.projection.UserFolioDetailsPanProjection;
import com.example.application.portfolio.repository.UserFolioDetailsRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserFolioDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserFolioDetailService.class);

    private final UserFolioDetailsRepository userFolioDetailsRepository;

    UserFolioDetailService(UserFolioDetailsRepository userFolioDetailsRepository) {
        this.userFolioDetailsRepository = userFolioDetailsRepository;
    }

    public List<UserFolioDetails> findByFoliosIn(List<UserFolioDetails> folios) {
        return userFolioDetailsRepository.findByUserCasDetails_FoliosIn(folios);
    }

    // if panKYC is NOT OK then PAN is not set. hence manually setting it.
    public void setPANIfNotSet(Long userCasID) {
        // find pan by id
        UserFolioDetailsPanProjection panProjection =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK");
        int rowsUpdated = userFolioDetailsRepository.updatePanByCasId(panProjection.getPan(), userCasID);
        log.debug("Updated {} rows with PAN", rowsUpdated);
    }
}
