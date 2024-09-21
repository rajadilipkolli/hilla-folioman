package com.app.folioman.mfschemes.entities;

import com.app.folioman.shared.Auditable;
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
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "mf_fund_scheme", schema = "mfschemes")
@Entity
public class MfFundScheme extends Auditable<String> implements Serializable {

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
    private Long amfiCode;
    private String isin;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_amc_id", nullable = false)
    private MfAmc amc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_category_id")
    private MFSchemeType mfSchemeType;

    @OneToMany(mappedBy = "mfScheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeNav> mfSchemeNavs = new ArrayList<>();

    @Version
    private Short version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public MfFundScheme setSid(int sid) {
        this.sid = sid;
        return this;
    }

    public String getName() {
        return name;
    }

    public MfFundScheme setName(String name) {
        this.name = name;
        return this;
    }

    public String getRta() {
        return rta;
    }

    public MfFundScheme setRta(String rta) {
        this.rta = rta;
        return this;
    }

    public String getPlan() {
        return plan;
    }

    public MfFundScheme setPlan(String plan) {
        this.plan = plan;
        return this;
    }

    public String getRtaCode() {
        return rtaCode;
    }

    public MfFundScheme setRtaCode(String rtaCode) {
        this.rtaCode = rtaCode;
        return this;
    }

    public String getAmcCode() {
        return amcCode;
    }

    public MfFundScheme setAmcCode(String amcCode) {
        this.amcCode = amcCode;
        return this;
    }

    public Long getAmfiCode() {
        return amfiCode;
    }

    public MfFundScheme setAmfiCode(Long amfiCode) {
        this.amfiCode = amfiCode;
        return this;
    }

    public String getIsin() {
        return isin;
    }

    public MfFundScheme setIsin(String isin) {
        this.isin = isin;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public MfFundScheme setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public MfFundScheme setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public MfAmc getAmc() {
        return amc;
    }

    public MfFundScheme setAmc(MfAmc amc) {
        this.amc = amc;
        return this;
    }

    public MFSchemeType getMfSchemeType() {
        return mfSchemeType;
    }

    public MfFundScheme setMfSchemeType(MFSchemeType mfSchemeType) {
        this.mfSchemeType = mfSchemeType;
        return this;
    }

    public List<MFSchemeNav> getMfSchemeNavs() {
        return mfSchemeNavs;
    }

    public MfFundScheme setMfSchemeNavs(List<MFSchemeNav> mfSchemeNavs) {
        this.mfSchemeNavs = mfSchemeNavs;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public MfFundScheme setVersion(Short version) {
        this.version = version;
        return this;
    }

    public MfFundScheme addSchemeNav(MFSchemeNav mfSchemeNav) {
        mfSchemeNavs.add(mfSchemeNav);
        mfSchemeNav.setMfScheme(this);
        return this;
    }
}
