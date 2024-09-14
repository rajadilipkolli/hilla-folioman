package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.repository.UserSchemeDetailsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserSchemeDetailsServiceInternal {

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;

    public UserSchemeDetailsServiceInternal(UserSchemeDetailsRepository userSchemeDetailsRepository) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
    }
}
