package com.app.folioman.portfolio.entities;

import com.app.folioman.shared.Auditable;
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
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(
        name = "user_scheme_details",
        schema = "portfolio",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_userschemedetailsentity",
                    columnNames = {"isin", "user_folio_id"})
        })
public class UserSchemeDetails extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_scheme_details_seq")
    @SequenceGenerator(name = "user_scheme_details_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "scheme", nullable = false)
    private String scheme;

    @Column(name = "isin")
    private String isin;

    private String advisor;

    private String rtaCode;

    private String rta;

    private String type;

    private Long amfi;

    @Column(name = "open")
    private String myopen;

    private String close;

    @Column(name = "close_calculated")
    private String closeCalculated;

    @Version
    private Short version;

    @ManyToOne
    @JoinColumn(name = "user_folio_id", nullable = false)
    private UserFolioDetails userFolioDetails;

    @OneToMany(mappedBy = "userSchemeDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<UserTransactionDetails> transactions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public UserSchemeDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public UserSchemeDetails setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getIsin() {
        return isin;
    }

    public UserSchemeDetails setIsin(String isin) {
        this.isin = isin;
        return this;
    }

    public String getAdvisor() {
        return advisor;
    }

    public UserSchemeDetails setAdvisor(String advisor) {
        this.advisor = advisor;
        return this;
    }

    public String getRtaCode() {
        return rtaCode;
    }

    public UserSchemeDetails setRtaCode(String rtaCode) {
        this.rtaCode = rtaCode;
        return this;
    }

    public String getRta() {
        return rta;
    }

    public UserSchemeDetails setRta(String rta) {
        this.rta = rta;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserSchemeDetails setType(String type) {
        this.type = type;
        return this;
    }

    public Long getAmfi() {
        return amfi;
    }

    public UserSchemeDetails setAmfi(Long amfi) {
        this.amfi = amfi;
        return this;
    }

    public String getMyopen() {
        return myopen;
    }

    public UserSchemeDetails setMyopen(String myopen) {
        this.myopen = myopen;
        return this;
    }

    public String getClose() {
        return close;
    }

    public UserSchemeDetails setClose(String close) {
        this.close = close;
        return this;
    }

    public String getCloseCalculated() {
        return closeCalculated;
    }

    public UserSchemeDetails setCloseCalculated(String closeCalculated) {
        this.closeCalculated = closeCalculated;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public UserSchemeDetails setVersion(Short version) {
        this.version = version;
        return this;
    }

    public UserFolioDetails getUserFolioDetails() {
        return userFolioDetails;
    }

    public UserSchemeDetails setUserFolioDetails(UserFolioDetails userFolioDetails) {
        this.userFolioDetails = userFolioDetails;
        return this;
    }

    public List<UserTransactionDetails> getTransactions() {
        return transactions;
    }

    public UserSchemeDetails setTransactions(List<UserTransactionDetails> transactions) {
        this.transactions = transactions;
        return this;
    }

    public void addTransaction(UserTransactionDetails userTransactionDetails) {
        this.transactions.add(userTransactionDetails);
        userTransactionDetails.setUserSchemeDetails(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserSchemeDetails userSchemeDetails = (UserSchemeDetails) o;
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
