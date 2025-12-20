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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "user_portfolio_value",
        schema = "portfolio",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_userportfoliovalue_date",
                    columnNames = {"date", "user_cas_details_id"})
        })
public class UserPortfolioValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_portfolio_value_seq")
    @SequenceGenerator(
            name = "user_portfolio_value_seq",
            sequenceName = "user_portfolio_value_seq",
            schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 30, scale = 2, nullable = false)
    private BigDecimal invested;

    @Column(precision = 30, scale = 2, nullable = false)
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof UserPortfolioValue that)) return false;

        return Objects.equals(getId(), that.getId())
                && Objects.equals(getDate(), that.getDate())
                && Objects.equals(getInvested(), that.getInvested())
                && Objects.equals(getValue(), that.getValue())
                && Objects.equals(getXirr(), that.getXirr())
                && Objects.equals(getLiveXirr(), that.getLiveXirr())
                && Objects.equals(getUserCasDetails(), that.getUserCasDetails());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getDate());
        result = 31 * result + Objects.hashCode(getInvested());
        result = 31 * result + Objects.hashCode(getValue());
        result = 31 * result + Objects.hashCode(getXirr());
        result = 31 * result + Objects.hashCode(getLiveXirr());
        result = 31 * result + Objects.hashCode(getUserCasDetails());
        return result;
    }
}
