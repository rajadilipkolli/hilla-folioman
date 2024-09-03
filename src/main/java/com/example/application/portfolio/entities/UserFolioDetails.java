package com.example.application.portfolio.entities;

import com.example.application.common.Auditable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(
        name = "user_folio_details",
        schema = "portfolio",
        indexes = {@Index(name = "user_details_idx_pan_id", columnList = "id, pan")})
public class UserFolioDetails extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_folio_details_seq")
    @SequenceGenerator(name = "user_folio_details_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "folio", nullable = false)
    private String folio;

    @Column(name = "amc", nullable = false)
    private String amc;

    @Column(name = "pan", nullable = false)
    private String pan;

    @Column(name = "kyc")
    private String kyc;

    @Column(name = "pan_kyc")
    private String panKyc;

    @OneToMany(mappedBy = "userFolioDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSchemeDetails> schemeEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_cas_details_id", nullable = false)
    private UserCASDetails userCasDetails;

    public Long getId() {
        return id;
    }

    public UserFolioDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFolio() {
        return folio;
    }

    public UserFolioDetails setFolio(String folio) {
        this.folio = folio;
        return this;
    }

    public String getAmc() {
        return amc;
    }

    public UserFolioDetails setAmc(String amc) {
        this.amc = amc;
        return this;
    }

    public String getPan() {
        return pan;
    }

    public UserFolioDetails setPan(String pan) {
        this.pan = pan;
        return this;
    }

    public String getKyc() {
        return kyc;
    }

    public UserFolioDetails setKyc(String kyc) {
        this.kyc = kyc;
        return this;
    }

    public String getPanKyc() {
        return panKyc;
    }

    public UserFolioDetails setPanKyc(String panKyc) {
        this.panKyc = panKyc;
        return this;
    }

    public List<UserSchemeDetails> getSchemeEntities() {
        return schemeEntities;
    }

    public UserFolioDetails setSchemeEntities(List<UserSchemeDetails> schemeEntities) {
        this.schemeEntities = schemeEntities;
        return this;
    }

    public UserCASDetails getUserCasDetails() {
        return userCasDetails;
    }

    public UserFolioDetails setUserCasDetails(UserCASDetails userCasDetails) {
        this.userCasDetails = userCasDetails;
        return this;
    }

    public void addSchemeEntity(UserSchemeDetails userSchemeDetailsEntity) {
        this.schemeEntities.add(userSchemeDetailsEntity);
        userSchemeDetailsEntity.setUserFolioDetails(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserFolioDetails that = (UserFolioDetails) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
