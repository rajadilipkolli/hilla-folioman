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
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "folio_scheme", schema = "portfolio")
class FolioSchemeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folio_scheme_gen")
    @SequenceGenerator(name = "folio_scheme_gen", sequenceName = "folio_scheme_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(precision = 20, scale = 2)
    private BigDecimal valuation;

    @Column(precision = 20, scale = 4)
    private BigDecimal xirr;

    private LocalDate valuationDate;

    @ManyToOne
    @JoinColumn(name = "user_folio_id", nullable = false)
    private UserFolioDetailsEntity userFolioDetails;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id", nullable = false)
    private UserSchemeDetailsEntity userSchemeDetails;

    public Long getId() {
        return id;
    }

    public FolioSchemeEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getValuation() {
        return valuation;
    }

    public FolioSchemeEntity setValuation(BigDecimal valuation) {
        this.valuation = valuation;
        return this;
    }

    public BigDecimal getXirr() {
        return xirr;
    }

    public FolioSchemeEntity setXirr(BigDecimal xirr) {
        this.xirr = xirr;
        return this;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public FolioSchemeEntity setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
        return this;
    }

    public UserFolioDetailsEntity getUserFolioDetails() {
        return userFolioDetails;
    }

    public FolioSchemeEntity setUserFolioDetailsEntity(UserFolioDetailsEntity userFolioDetails) {
        this.userFolioDetails = userFolioDetails;
        return this;
    }

    public UserSchemeDetailsEntity getUserSchemeDetails() {
        return userSchemeDetails;
    }

    public FolioSchemeEntity setUserSchemeDetailsEntity(UserSchemeDetailsEntity userSchemeDetails) {
        this.userSchemeDetails = userSchemeDetails;
        return this;
    }
}
