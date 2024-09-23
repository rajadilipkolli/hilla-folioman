package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.FolioScheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolioSchemeRepository extends JpaRepository<FolioScheme, Long> {

    List<FolioScheme> findByUserFolioDetails_Id(Long id);
}
