package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.repository.UserFolioDetailsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserFolioDetailService {

    private final UserFolioDetailsRepository userFolioDetailsRepository;

    public UserFolioDetailService(UserFolioDetailsRepository userFolioDetailsRepository) {
        this.userFolioDetailsRepository = userFolioDetailsRepository;
    }

    public List<UserFolioDetails> findByFoliosIn(List<UserFolioDetails> folios) {
        return userFolioDetailsRepository.findByUserCasDetails_FoliosIn(folios);
    }
}
