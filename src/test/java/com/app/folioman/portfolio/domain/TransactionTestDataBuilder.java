package com.app.folioman.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionTestDataBuilder {
    private final UserCasDetailsEntity casDetails;
    private UserFolioDetailsEntity currentFolio;
    private UserSchemeDetailsEntity currentScheme;

    public TransactionTestDataBuilder() {
        casDetails = new UserCasDetailsEntity();
        casDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        casDetails.setFileTypeEnum(FileTypeEnum.CAMS);
    }

    public static TransactionTestDataBuilder builder() {
        return new TransactionTestDataBuilder();
    }

    public TransactionTestDataBuilder withInvestor(String email, String name) {
        InvestorInfoEntity investor = new InvestorInfoEntity();
        investor.setEmail(email);
        investor.setName(name);
        casDetails.setInvestorInfoEntity(investor);
        return this;
    }

    public TransactionTestDataBuilder withFolio(String folioNumber, String amc, String pan) {
        currentFolio = new UserFolioDetailsEntity();
        currentFolio.setFolio(folioNumber);
        currentFolio.setAmc(amc);
        currentFolio.setPan(pan);
        casDetails.addFolioEntity(currentFolio);
        return this;
    }

    public TransactionTestDataBuilder withScheme(String name, String isin, Long amfiCode) {
        if (currentFolio == null) {
            throw new IllegalStateException("Must call withFolio() before withScheme()");
        }
        currentScheme = new UserSchemeDetailsEntity();
        currentScheme.setScheme(name);
        currentScheme.setIsin(isin);
        currentScheme.setAmfi(amfiCode);
        currentFolio.addScheme(currentScheme);
        return this;
    }

    public TransactionTestDataBuilder addTransaction(
            LocalDate date, TransactionType type, BigDecimal amount, Double units, Double nav, Double balance) {
        if (currentScheme == null) {
            throw new IllegalStateException("Must call withScheme() before addTransaction()");
        }
        UserTransactionDetailsEntity transaction = new UserTransactionDetailsEntity();
        transaction.setTransactionDate(date);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setUnits(units);
        transaction.setNav(nav);
        transaction.setBalance(balance);
        currentScheme.addTransaction(transaction);
        return this;
    }

    public UserCasDetailsEntity build() {
        return casDetails;
    }
}
