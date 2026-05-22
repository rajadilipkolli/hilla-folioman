package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        name = "user_scheme_details",
        schema = "portfolio",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_userschemedetailsentity",
                    columnNames = {"isin", "user_folio_id"})
        })
class UserSchemeDetailsEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_scheme_details_seq")
    @SequenceGenerator(name = "user_scheme_details_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "scheme", nullable = false)
    private String scheme;

    @Column(name = "isin")
    private @Nullable String isin;

    private @Nullable String advisor;

    private @Nullable String rtaCode;

    private @Nullable String rta;

    private @Nullable String type;

    private @Nullable Long amfi;

    @Column(name = "open")
    private @Nullable String myopen;

    private @Nullable String close;

    @Column(name = "close_calculated")
    private @Nullable String closeCalculated;

    @ManyToOne
    @JoinColumn(name = "user_folio_id", nullable = false)
    private UserFolioDetailsEntity userFolioDetails;

    @OneToMany(mappedBy = "userSchemeDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<UserTransactionDetailsEntity> transactions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public UserSchemeDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public UserSchemeDetailsEntity setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getIsin() {
        return isin;
    }

    public UserSchemeDetailsEntity setIsin(@Nullable String isin) {
        this.isin = isin;
        return this;
    }

    public String getAdvisor() {
        return advisor;
    }

    public UserSchemeDetailsEntity setAdvisor(@Nullable String advisor) {
        this.advisor = advisor;
        return this;
    }

    public String getRtaCode() {
        return rtaCode;
    }

    public UserSchemeDetailsEntity setRtaCode(@Nullable String rtaCode) {
        this.rtaCode = rtaCode;
        return this;
    }

    public String getRta() {
        return rta;
    }

    public UserSchemeDetailsEntity setRta(@Nullable String rta) {
        this.rta = rta;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserSchemeDetailsEntity setType(@Nullable String type) {
        this.type = type;
        return this;
    }

    public @Nullable Long getAmfi() {
        return amfi;
    }

    public UserSchemeDetailsEntity setAmfi(@Nullable Long amfi) {
        this.amfi = amfi;
        return this;
    }

    public String getMyopen() {
        return myopen;
    }

    public UserSchemeDetailsEntity setMyopen(@Nullable String myopen) {
        this.myopen = myopen;
        return this;
    }

    public String getClose() {
        return close;
    }

    public UserSchemeDetailsEntity setClose(@Nullable String close) {
        this.close = close;
        return this;
    }

    public String getCloseCalculated() {
        return closeCalculated;
    }

    public UserSchemeDetailsEntity setCloseCalculated(@Nullable String closeCalculated) {
        this.closeCalculated = closeCalculated;
        return this;
    }

    public UserFolioDetailsEntity getUserFolioDetails() {
        return userFolioDetails;
    }

    public UserSchemeDetailsEntity setUserFolioDetails(UserFolioDetailsEntity userFolioDetails) {
        this.userFolioDetails = userFolioDetails;
        return this;
    }

    public List<UserTransactionDetailsEntity> getTransactions() {
        return transactions;
    }

    public UserSchemeDetailsEntity setTransactions(List<UserTransactionDetailsEntity> transactions) {
        this.transactions = transactions;
        return this;
    }

    public void addTransaction(UserTransactionDetailsEntity userTransactionDetailsEntity) {
        this.transactions.add(userTransactionDetailsEntity);
        userTransactionDetailsEntity.setUserSchemeDetails(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserSchemeDetailsEntity userSchemeDetails = (UserSchemeDetailsEntity) o;
        return isin != null
                && Objects.equals(isin, userSchemeDetails.isin)
                && Objects.equals(
                        userFolioDetails.getId(),
                        userSchemeDetails.getUserFolioDetails().getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
