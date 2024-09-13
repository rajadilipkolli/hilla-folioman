package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserCASDetails;
import com.example.application.portfolio.repository.UserCASDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserCASDetailsService {

    private final UserCASDetailsRepository userCASDetailsRepository;

    public UserCASDetailsService(UserCASDetailsRepository userCASDetailsRepository) {
        this.userCASDetailsRepository = userCASDetailsRepository;
    }

    @Transactional
    public UserCASDetails saveEntity(UserCASDetails userCASDetails) {
        return userCASDetailsRepository.save(userCASDetails);
    }
}
