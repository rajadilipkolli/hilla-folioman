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
        name = "user_folio_value",
        schema = "portfolio",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_userfoliovalue_date_folio",
                    columnNames = {"date", "user_folio_details_id"})
        })
@SuppressWarnings("NullAway.Init")
class UserFolioValueEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_folio_value_seq")
    @SequenceGenerator(name = "user_folio_value_seq", sequenceName = "user_folio_value_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 30, scale = 2, nullable = false)
    private BigDecimal invested;

    @Column(precision = 30, scale = 2, nullable = false)
    private BigDecimal value;

    @ManyToOne
    @JoinColumn(name = "user_folio_details_id", nullable = false)
    private UserFolioDetailsEntity userFolioDetailsEntity;

    public Long getId() {
        return id;
    }

    public UserFolioValueEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public UserFolioValueEntity setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public UserFolioValueEntity setInvested(BigDecimal invested) {
        this.invested = invested;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public UserFolioValueEntity setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public UserFolioDetailsEntity getUserFolioDetailsEntity() {
        return userFolioDetailsEntity;
    }

    public UserFolioValueEntity setUserFolioDetailsEntity(UserFolioDetailsEntity userFolioDetailsEntity) {
        this.userFolioDetailsEntity = userFolioDetailsEntity;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFolioValueEntity that)) return false;

        return Objects.equals(getDate(), that.getDate())
                && getUserFolioDetailsEntity() != null
                && that.getUserFolioDetailsEntity() != null
                && Objects.equals(
                        getUserFolioDetailsEntity().getId(),
                        that.getUserFolioDetailsEntity().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getDate(),
                getUserFolioDetailsEntity() != null
                        ? getUserFolioDetailsEntity().getId()
                        : null);
    }
}
