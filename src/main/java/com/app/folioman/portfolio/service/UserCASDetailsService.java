package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.repository.UserCASDetailsRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserCASDetailsService {

    private final UserCASDetailsRepository userCASDetailsRepository;
    private final UserFolioDetailService userFolioDetailService;
    private final UserSchemeDetailServiceImpl userSchemeDetailService;

    UserCASDetailsService(
            UserCASDetailsRepository userCASDetailsRepository,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailServiceImpl userSchemeDetailService) {
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.userFolioDetailService = userFolioDetailService;
        this.userSchemeDetailService = userSchemeDetailService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCASDetails saveEntity(UserCASDetails userCASDetails) {
        return userCASDetailsRepository.save(userCASDetails);
    }

    public UserCASDetails findByInvestorEmailAndName(String email, String name) {
        UserCASDetails byInvestorEmailAndName = userCASDetailsRepository.findByInvestorEmailAndName(email, name);
        // Get all folio details with associated schemes
        List<UserFolioDetails> byFoliosIn = userFolioDetailService.findByFoliosIn(byInvestorEmailAndName.getFolios());
        // get all distinct schemes associated with folio
        List<UserSchemeDetails> userSchemeDetails = byFoliosIn.stream()
                .map(UserFolioDetails::getSchemes)
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<UserSchemeDetails> bySchemesIn = userSchemeDetailService.findBySchemesIn(userSchemeDetails);
        // Map schemes to the corresponding folios
        Map<UserFolioDetails, List<UserSchemeDetails>> folioToSchemesMap = byFoliosIn.stream()
                .collect(Collectors.toMap(folio -> folio, folio -> bySchemesIn.stream()
                        .filter(scheme ->
                                scheme.getUserFolioDetails().getFolio().equals(folio.getFolio()))
                        .collect(Collectors.toList())));

        // Set the schemes in each folio
        byFoliosIn.forEach(folio -> folio.setSchemes(folioToSchemesMap.get(folio)));

        // Update the folios for the casDetails
        byInvestorEmailAndName.setFolios(byFoliosIn);
        return byInvestorEmailAndName;
    }

    public List<PortfolioDetailsProjection> getPortfolioDetailsByPanAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);
    }
}
