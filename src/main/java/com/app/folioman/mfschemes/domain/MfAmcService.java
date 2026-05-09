package com.app.folioman.mfschemes.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.FuzzyScore;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class MfAmcService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfAmcService.class);

    private final MfAmcCacheService mfAmcCacheService;

    // Local cache for AMC entities by name for faster lookup in find-or-create operations
    private final ConcurrentHashMap<String, MfAmcEntity> amcNameCache = new ConcurrentHashMap<>();

    MfAmcService(MfAmcCacheService mfAmcCacheService) {
        this.mfAmcCacheService = mfAmcCacheService;
    }

    public @Nullable MfAmcEntity findByCode(String code) {
        return this.mfAmcCacheService.findByCode(code);
    }

    public MfAmcEntity saveMfAmc(MfAmcEntity amc) {
        MfAmcEntity savedAmc = this.mfAmcCacheService.saveMfAmc(amc);
        // Update local cache after saving
        String name = savedAmc.getName();
        if (name != null) {
            amcNameCache.put(name, savedAmc);
        }
        return savedAmc;
    }

    public @Nullable MfAmcEntity findByName(String amcName) {
        // First check local cache
        MfAmcEntity cachedAmc = amcNameCache.get(amcName);
        if (cachedAmc != null) {
            return cachedAmc;
        }

        // If not in cache, try database lookup
        MfAmcEntity byNameIgnoreCase = mfAmcCacheService.findByName(amcName);
        if (byNameIgnoreCase != null) {
            // Update cache and return
            amcNameCache.put(amcName, byNameIgnoreCase);
            return byNameIgnoreCase;
        }

        // As last resort, try to find closest match
        MfAmcEntity closestMatch = findClosestMatch(amcName);
        if (closestMatch != null) {
            // Cache the result of closest match for future lookups
            amcNameCache.put(amcName, closestMatch);
        }
        return closestMatch;
    }

    /**
     * Find AMCs by text search terms using PostgreSQL's full-text search
     *
     * @param searchTerms Space-separated search terms
     * @return List of matching AMC entities
     */
    public List<MfAmcEntity> findByTextSearch(String searchTerms) {
        List<MfAmcEntity> results = mfAmcCacheService.findByTextSearch(searchTerms);
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
    public List<MfAmcEntity> findBySearchTerms(String searchTerms) {
        List<MfAmcEntity> textSearchResults = findByTextSearch(searchTerms);

        // If text search found results, return them
        if (!textSearchResults.isEmpty()) {
            return textSearchResults;
        }

        // Fall back to fuzzy search with individual terms
        List<MfAmcEntity> mfAmcList = mfAmcCacheService.findAllAmcs();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);

        // Return AMCs with fuzzy score above threshold, sorted by score
        return mfAmcList.stream()
                .map(amc -> new Object[] {amc, fuzzyScore.fuzzyScore(amc.getName(), searchTerms)})
                .filter(pair -> (int) pair[1] > 20) // Filter out low-quality matches (score ≤20)
                .sorted(Comparator.comparingInt(pair -> -1 * (int) pair[1])) // Sort by score desc
                .map(pair -> (MfAmcEntity) pair[0])
                .limit(10) // Limit to top 10 matches
                .collect(Collectors.toList());
    }

    private @Nullable MfAmcEntity findClosestMatch(String amcName) {
        List<MfAmcEntity> mfAmcList = mfAmcCacheService.findAllAmcs();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);
        return mfAmcList.stream()
                .max(Comparator.comparingInt(entry -> fuzzyScore.fuzzyScore(amcName, entry.getName())))
                .orElse(null);
    }

    public MfAmcEntity findOrCreateByName(String amcName) {
        // First check if AMC exists
        MfAmcEntity amc = findByName(amcName);
        if (amc != null) {
            return amc;
        }

        // Use computeIfAbsent for atomic check-and-create operation
        return amcNameCache.computeIfAbsent(amcName, name -> {
            // Double-check in database to avoid race conditions with other threads
            MfAmcEntity existingAmc = mfAmcCacheService.findByName(name);
            if (existingAmc != null) {
                return existingAmc;
            }

            // Create and save new AMC
            MfAmcEntity newAmc = new MfAmcEntity();
            newAmc.setName(name);
            newAmc.setCode(name);
            return mfAmcCacheService.saveMfAmc(newAmc);
        });
    }
}
