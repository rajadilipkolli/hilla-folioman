package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.FuzzyScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MfAmcService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfAmcService.class);

    private final MfAmcCacheService mfAmcCacheService;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public MfAmcService(MfAmcCacheService mfAmcCacheService) {
        this.mfAmcCacheService = mfAmcCacheService;
    }

    public MfAmc findByCode(String code) {
        return this.mfAmcCacheService.findByCode(code);
    }

    public MfAmc saveMfAmc(MfAmc amc) {
        return this.mfAmcCacheService.saveMfAmc(amc);
    }

    public MfAmc findByName(String amcName) {
        MfAmc byNameIgnoreCase = mfAmcCacheService.findByName(amcName);
        return byNameIgnoreCase != null ? byNameIgnoreCase : findClosestMatch(amcName);
    }

    /**
     * Find AMCs by text search terms using PostgreSQL's full-text search
     *
     * @param searchTerms Space-separated search terms
     * @return List of matching AMC entities
     */
    public List<MfAmc> findByTextSearch(String searchTerms) {
        List<MfAmc> results = mfAmcCacheService.findByTextSearch(searchTerms);
        if (results.isEmpty() && searchTerms.contains(" ")) {
            // Try with individual terms if the combined search didn't yield results
            String singleTermQuery = searchTerms.split("\\s+")[0];
            LOGGER.debug("Trying single term search with: {}", singleTermQuery);
            return mfAmcCacheService.findByTextSearch(singleTermQuery);
        }
        return results;
    }

    /**
     * Find AMCs by combining text search with fuzzy matching
     * This method first tries text search and falls back to fuzzy matching if needed
     *
     * @param searchTerms Search terms to look for
     * @return List of matching AMCs
     */
    public List<MfAmc> findBySearchTerms(String searchTerms) {
        List<MfAmc> textSearchResults = findByTextSearch(searchTerms);

        // If text search found results, return them
        if (!textSearchResults.isEmpty()) {
            return textSearchResults;
        }

        // Fall back to fuzzy search with individual terms
        List<MfAmc> mfAmcList = mfAmcCacheService.findAllAmcs();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);

        // Return AMCs with fuzzy score above threshold, sorted by score
        return mfAmcList.stream()
                .map(amc -> new Object[] {amc, fuzzyScore.fuzzyScore(searchTerms, amc.getName())})
                .filter(pair -> (int) pair[1] > 5) // Filter out zero scores
                .sorted(Comparator.comparingInt(pair -> -1 * (int) pair[1])) // Sort by score desc
                .map(pair -> (MfAmc) pair[0])
                .limit(10) // Limit to top 10 matches
                .collect(Collectors.toList());
    }

    private MfAmc findClosestMatch(String amcName) {
        List<MfAmc> mfAmcList = mfAmcCacheService.findAllAmcs();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);
        return mfAmcList.stream()
                .max(Comparator.comparingInt(entry -> fuzzyScore.fuzzyScore(amcName, entry.getName())))
                .orElse(null);
    }

    public MfAmc findOrCreateByName(String amcName) {
        MfAmc amc = findByName(amcName);
        if (amc == null) {
            reentrantLock.lock();
            try {
                // Recheck cache after acquiring the lock
                amc = mfAmcCacheService.findByName(amcName);
                if (amc == null) {
                    amc = new MfAmc();
                    amc.setName(amcName);
                    amc.setCode(amcName);
                    amc = mfAmcCacheService.saveMfAmc(amc);
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        return amc;
    }
}
