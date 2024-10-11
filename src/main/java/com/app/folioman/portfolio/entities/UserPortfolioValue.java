package com.app.folioman.portfolio.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_portfolio_value", schema = "portfolio")
public class UserPortfolioValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_portfolio_value_seq")
    @SequenceGenerator(name = "user_portfolio_value_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate date;

    @Column(precision = 30, scale = 2)
    private BigDecimal invested;

    @Column(precision = 30, scale = 2)
    private BigDecimal value;

    @Column(precision = 30, scale = 2)
    private BigDecimal xirr;

    @Column(precision = 30, scale = 2)
    private BigDecimal liveXirr;

    @ManyToOne
    @JoinColumn(name = "user_cas_details_id", nullable = false)
    private UserCASDetails userCasDetails;

    public Long getId() {
        return id;
    }

    public UserPortfolioValue setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public UserPortfolioValue setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public UserPortfolioValue setInvested(BigDecimal invested) {
        this.invested = invested;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public UserPortfolioValue setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public BigDecimal getXirr() {
        return xirr;
    }

    public UserPortfolioValue setXirr(BigDecimal xirr) {
        this.xirr = xirr;
        return this;
    }

    public BigDecimal getLiveXirr() {
        return liveXirr;
    }

    public UserPortfolioValue setLiveXirr(BigDecimal liveXirr) {
        this.liveXirr = liveXirr;
        return this;
    }

    public UserCASDetails getUserCasDetails() {
        return userCasDetails;
    }

    public UserPortfolioValue setUserCasDetails(UserCASDetails userCasDetails) {
        this.userCasDetails = userCasDetails;
        return this;
    }
}
