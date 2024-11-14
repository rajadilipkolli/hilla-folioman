package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.repository.UserCASDetailsRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserCASDetailsService {

    private final UserCASDetailsRepository userCASDetailsRepository;

    UserCASDetailsService(UserCASDetailsRepository userCASDetailsRepository) {
        this.userCASDetailsRepository = userCASDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCASDetails saveEntity(UserCASDetails userCASDetails) {
        return userCASDetailsRepository.save(userCASDetails);
    }

    public UserCASDetails findByInvestorEmailAndName(String email, String name) {
        return userCASDetailsRepository.findByInvestorEmailAndName(email, name);
    }

    public List<PortfolioDetailsProjection> getPortfolioDetailsByPanAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);
    }
}
