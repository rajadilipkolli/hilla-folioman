package com.app.folioman.auth.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findByUsernameForUpdate(String username);
}
