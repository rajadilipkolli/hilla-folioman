package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class MfAmcCacheService {

    private final MfAmcRepository mfAmcRepository;

    MfAmcCacheService(MfAmcRepository mfAmcRepository) {
        this.mfAmcRepository = mfAmcRepository;
    }

    @Cacheable(value = "findByAMCName", key = "#amcName", unless = "#result == null")
    public MfAmc findByName(String amcName) {
        return mfAmcRepository.findByNameIgnoreCase(amcName.toUpperCase(Locale.ENGLISH));
    }

    @Cacheable(value = "findByAMCCode", key = "#code", unless = "#result == null")
    public MfAmc findByCode(String code) {
        return this.mfAmcRepository.findByCode(code);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MfAmc saveMfAmc(MfAmc amc) {
        return mfAmcRepository.save(amc);
    }

    public List<MfAmc> findAllAmcs() {
        return this.mfAmcRepository.findAll();
    }

    /**
     * Find AMCs by using full-text search on name and code
     *
     * @param searchTerms Space-separated search terms
     * @return List of matching AMCs
     */
    @Cacheable(value = "findAMCsByTextSearch", key = "#searchTerms", unless = "#result.isEmpty()")
    public List<MfAmc> findByTextSearch(String searchTerms) {
        // Convert space-separated terms to PostgreSQL ts_query format (term1 & term2 & ...)
        String tsQueryFormat = Arrays.stream(
                        searchTerms.strip().replaceAll("\\s+", " ").split("\\s+"))
                .map(term -> term.toLowerCase(Locale.ENGLISH))
                .map(term -> term.replaceAll("[^a-zA-Z0-9]", "")) // Remove special characters
                .filter(term -> !term.isEmpty())
                .collect(Collectors.joining(" & "));

        if (tsQueryFormat.isEmpty()) {
            return List.of();
        }

        return mfAmcRepository.findByTextSearch(tsQueryFormat);
    }
}
