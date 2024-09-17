package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserTransactionDetails;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTransactionDetailsRepository extends JpaRepository<UserTransactionDetails, Long> {

    @Query(
            """
            select count (u.id) from UserTransactionDetails u
            where upper(u.userSchemeDetails.userFolioDetails.userCasDetails.investorInfo.email) = upper(:email) and u.userSchemeDetails.userFolioDetails.userCasDetails.investorInfo.name = :name and u.transactionDate >= :fromTransactionDate and u.transactionDate <= :toTransactionDate
            """)
    Long findAllTransactionByEmailAndNameAndInRange(
            @Param("email") String email,
            @Param("name") String name,
            @Param("fromTransactionDate") LocalDate fromTransactionDate,
            @Param("toTransactionDate") LocalDate toTransactionDate);
}
