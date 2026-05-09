package com.app.folioman.portfolio.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface FolioSchemeRepository extends JpaRepository<FolioSchemeEntity, Long> {

    /**
     * Find a FolioSchemeEntity by the user scheme detail ID
     *
     * @param userSchemeDetailId the ID of the user scheme detail
     * @return the FolioSchemeEntity if found
     */
    Optional<FolioSchemeEntity> findByUserSchemeDetails_Id(Long userSchemeDetailId);

    List<FolioSchemeEntity> findByUserFolioDetails_Id(Long id);
}
