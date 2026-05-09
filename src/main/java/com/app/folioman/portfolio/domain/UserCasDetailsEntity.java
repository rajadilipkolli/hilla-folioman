package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_cas_details", schema = "portfolio")
public class UserCasDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usercasdetails_seq")
    @SequenceGenerator(name = "usercasdetails_seq", schema = "portfolio", sequenceName = "user_cas_details_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cas_type", nullable = false)
    private CasTypeEnum casTypeEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileTypeEnum fileTypeEnum;

    @OneToOne(mappedBy = "userCasDetailsEntity", cascade = CascadeType.ALL, optional = false)
    private InvestorInfo investorInfo;

    @OneToMany(mappedBy = "userCasDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFolioDetailsEntity> folios = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public UserCasDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public CasTypeEnum getCasTypeEnum() {
        return casTypeEnum;
    }

    public UserCasDetailsEntity setCasTypeEnum(CasTypeEnum casTypeEnum) {
        this.casTypeEnum = casTypeEnum;
        return this;
    }

    public FileTypeEnum getFileTypeEnum() {
        return fileTypeEnum;
    }

    public UserCasDetailsEntity setFileTypeEnum(FileTypeEnum fileTypeEnum) {
        this.fileTypeEnum = fileTypeEnum;
        return this;
    }

    public List<UserFolioDetailsEntity> getFolios() {
        return folios;
    }

    public UserCasDetailsEntity setFolios(List<UserFolioDetailsEntity> folios) {
        this.folios = folios;
        return this;
    }

    public InvestorInfo getInvestorInfo() {
        return investorInfo;
    }

    public UserCasDetailsEntity setInvestorInfo(InvestorInfo investorInfo) {
        if (investorInfo == null) {
            if (this.investorInfo != null) {
                this.investorInfo.setUserCasDetailsEntity(null);
            }
        } else {
            investorInfo.setUserCasDetailsEntity(this);
        }
        this.investorInfo = investorInfo;
        return this;
    }

    public void addFolioEntity(UserFolioDetailsEntity userFolioDetailsEntity) {
        this.folios.add(userFolioDetailsEntity);
        userFolioDetailsEntity.setUserCasDetailsEntity(this);
    }
}
