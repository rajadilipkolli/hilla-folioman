package com.example.application.user.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.DynamicUpdate;

@Table(name = "mf_scheme")
@Entity
@DynamicUpdate
public class MFScheme extends Auditable<String> implements Serializable {

    @Id
    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    private String payOut;

    @Column(name = "fund_house")
    private String fundHouse;

    @Column(name = "scheme_name", nullable = false)
    private String schemeName;

    @Column(name = "scheme_name_alias")
    private String schemeNameAlias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_type_id")
    private MFSchemeType mfSchemeTypeEntity = null;

    @OneToMany(mappedBy = "mfSchemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeNav> mfSchemeNavEntities = new ArrayList<>();

    @Version
    private Short version;

    public Long getSchemeId() {
        return schemeId;
    }

    public MFScheme setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
        return this;
    }

    public String getPayOut() {
        return payOut;
    }

    public MFScheme setPayOut(String payOut) {
        this.payOut = payOut;
        return this;
    }

    public String getFundHouse() {
        return fundHouse;
    }

    public MFScheme setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
        return this;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MFScheme setSchemeName(String schemeName) {
        this.schemeName = schemeName;
        return this;
    }

    public String getSchemeNameAlias() {
        return schemeNameAlias;
    }

    public MFScheme setSchemeNameAlias(String schemeNameAlias) {
        this.schemeNameAlias = schemeNameAlias;
        return this;
    }

    public MFSchemeType getMfSchemeTypeEntity() {
        return mfSchemeTypeEntity;
    }

    public MFScheme setMfSchemeTypeEntity(MFSchemeType mfSchemeTypeEntity) {
        this.mfSchemeTypeEntity = mfSchemeTypeEntity;
        return this;
    }

    public List<MFSchemeNav> getMfSchemeNavEntities() {
        return mfSchemeNavEntities;
    }

    public MFScheme setMfSchemeNavEntities(List<MFSchemeNav> mfSchemeNavEntities) {
        this.mfSchemeNavEntities = mfSchemeNavEntities;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public MFScheme setVersion(Short version) {
        this.version = version;
        return this;
    }

    public MFScheme addSchemeNav(MFSchemeNav mfSchemeNavEntity) {
        mfSchemeNavEntities.add(mfSchemeNavEntity);
        mfSchemeNavEntity.setMfSchemeEntity(this);
        return this;
    }
}
