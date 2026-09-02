package com.app.folioman.auth.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing refresh tokens.
 * Provides methods for finding, deleting, and managing refresh tokens for user authentication.
 */
@Repository
interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    /**
     * Finds a refresh token by its token value.
     *
     * @param token The token value to search for
     * @return Optional containing the refresh token if found, empty otherwise
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Finds all non-revoked refresh tokens for a specific user.
     *
     * @param userId The user ID to search for
     * @return List of non-revoked refresh tokens for the user
     */
    List<RefreshTokenEntity> findAllByUserIdAndRevokedFalse(Long userId);

    /**
     * Deletes all refresh tokens for a specific user.
     *
     * @param userId The user ID whose tokens should be deleted
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshTokenEntity r where r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
