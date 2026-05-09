package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.BaseEntity;
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
class UserPortfolioValueEntity extends BaseEntity {

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
    private UserCasDetailsEntity userCasDetailsEntity;

    public Long getId() {
        return id;
    }

    public UserPortfolioValueEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public UserPortfolioValueEntity setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public UserPortfolioValueEntity setInvested(BigDecimal invested) {
        this.invested = invested;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public UserPortfolioValueEntity setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public BigDecimal getXirr() {
        return xirr;
    }

    public UserPortfolioValueEntity setXirr(BigDecimal xirr) {
        this.xirr = xirr;
        return this;
    }

    public BigDecimal getLiveXirr() {
        return liveXirr;
    }

    public UserPortfolioValueEntity setLiveXirr(BigDecimal liveXirr) {
        this.liveXirr = liveXirr;
        return this;
    }

    public UserCasDetailsEntity getUserCasDetails() {
        return userCasDetailsEntity;
    }

    public UserPortfolioValueEntity setUserCasDetails(UserCasDetailsEntity userCasDetailsEntity) {
        this.userCasDetailsEntity = userCasDetailsEntity;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPortfolioValueEntity that)) return false;

        return Objects.equals(getDate(), that.getDate())
                && getUserCasDetails() != null
                && that.getUserCasDetails() != null
                && Objects.equals(
                        getUserCasDetails().getId(), that.getUserCasDetails().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getDate(), getUserCasDetails() != null ? getUserCasDetails().getId() : null);
    }
}
