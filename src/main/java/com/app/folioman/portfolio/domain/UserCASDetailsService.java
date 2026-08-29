package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.projection.PortfolioDetailsProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserCASDetailsService {

    private final UserCASDetailsRepository userCasDetailsRepository;

    UserCASDetailsService(UserCASDetailsRepository userCasDetailsRepository) {
        this.userCasDetailsRepository = userCasDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCasDetailsEntity saveEntity(UserCasDetailsEntity userCasDetailsEntity) {
        return userCasDetailsRepository.save(userCasDetailsEntity);
    }

    public Optional<UserCasDetailsEntity> findByInvestorEmailAndName(String email, String name) {
        return userCasDetailsRepository.findByInvestorEmailAndName(email, name);
    }

    public List<PortfolioDetailsProjection> getPortfolioDetailsByPanAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return userCasDetailsRepository.getPortfolioDetails(panNumber, asOfDate);
    }
}
