package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.repository.UserSchemeDetailsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserSchemeDetailsService {

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final UserSchemeDetailsServiceDelegate userSchemeDetailsServiceDelegate;

    UserSchemeDetailsService(
            UserSchemeDetailsRepository userSchemeDetailsRepository,
            UserSchemeDetailsServiceDelegate userSchemeDetailsServiceDelegate) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.userSchemeDetailsServiceDelegate = userSchemeDetailsServiceDelegate;
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
    }

    public void setUserSchemeAMFIIfNull() {
        userSchemeDetailsServiceDelegate.setUserSchemeAMFIIfNull();
    }

    public void loadHistoricalDataIfNotExists() {
        userSchemeDetailsServiceDelegate.loadHistoricalDataIfNotExists();
    }
}
