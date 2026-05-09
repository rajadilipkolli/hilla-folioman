package com.app.folioman.mfschemes.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "mf_fund_scheme", schema = "mfschemes")
@Entity
class MfFundSchemeEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mf_fund_scheme_gen")
    @SequenceGenerator(name = "mf_fund_scheme_gen", sequenceName = "mf_fund_scheme_seq", schema = "mfschemes")
    @Column(name = "id", nullable = false)
    private Long id;

    private int sid;
    private String name;
    private String rta;
    private String plan;
    private String rtaCode;
    private String amcCode;

    @Column(unique = true)
    private Long amfiCode;

    private String isin;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_amc_id", nullable = false)
    private MfAmcEntity amc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_category_id")
    private MFSchemeTypeEntity mfSchemeTypeEntity;

    @OneToMany(mappedBy = "mfFundSchemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeNavEntity> mfSchemeNavs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public MfFundSchemeEntity setSid(int sid) {
        this.sid = sid;
        return this;
    }

    public String getName() {
        return name;
    }

    public MfFundSchemeEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getRta() {
        return rta;
    }

    public MfFundSchemeEntity setRta(String rta) {
        this.rta = rta;
        return this;
    }

    public String getPlan() {
        return plan;
    }

    public MfFundSchemeEntity setPlan(String plan) {
        this.plan = plan;
        return this;
    }

    public String getRtaCode() {
        return rtaCode;
    }

    public MfFundSchemeEntity setRtaCode(String rtaCode) {
        this.rtaCode = rtaCode;
        return this;
    }

    public String getAmcCode() {
        return amcCode;
    }

    public MfFundSchemeEntity setAmcCode(String amcCode) {
        this.amcCode = amcCode;
        return this;
    }

    public Long getAmfiCode() {
        return amfiCode;
    }

    public MfFundSchemeEntity setAmfiCode(Long amfiCode) {
        this.amfiCode = amfiCode;
        return this;
    }

    public String getIsin() {
        return isin;
    }

    public MfFundSchemeEntity setIsin(String isin) {
        this.isin = isin;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public MfFundSchemeEntity setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public MfFundSchemeEntity setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public MfAmcEntity getAmc() {
        return amc;
    }

    public MfFundSchemeEntity setAmc(MfAmcEntity amc) {
        this.amc = amc;
        return this;
    }

    public MFSchemeTypeEntity getMfSchemeTypeEntity() {
        return mfSchemeTypeEntity;
    }

    public MfFundSchemeEntity setMfSchemeTypeEntity(MFSchemeTypeEntity mfSchemeType) {
        this.mfSchemeTypeEntity = mfSchemeType;
        return this;
    }

    public List<MFSchemeNavEntity> getMfSchemeNavs() {
        return mfSchemeNavs;
    }

    public MfFundSchemeEntity setMfSchemeNavs(List<MFSchemeNavEntity> mfSchemeNavs) {
        this.mfSchemeNavs = mfSchemeNavs;
        return this;
    }

    public MfFundSchemeEntity addSchemeNav(MFSchemeNavEntity mfSchemeNav) {
        if (mfSchemeNav == null) {
            throw new IllegalArgumentException("mfSchemeNav cannot be null");
        }
        mfSchemeNavs.add(mfSchemeNav);
        mfSchemeNav.setMfFundSchemeEntity(this);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MfFundSchemeEntity)) return false;
        MfFundSchemeEntity that = (MfFundSchemeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
