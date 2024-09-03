package com.example.application.portfolio.entities;

import com.example.application.common.Auditable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_cas_details", schema = "portfolio")
public class UserCASDetails extends Auditable<String> implements Serializable {

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

    @OneToOne(mappedBy = "userCasDetails", cascade = CascadeType.ALL, optional = false)
    private InvestorInfo investorInfo;

    @OneToMany(mappedBy = "userCasDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFolioDetails> folios = new ArrayList<>();

    public List<UserFolioDetails> getFolios() {
        return folios;
    }

    public void setFolios(List<UserFolioDetails> folios) {
        this.folios = folios;
    }

    public InvestorInfo getInvestorInfo() {
        return investorInfo;
    }

    public void setInvestorInfo(InvestorInfo investorInfo) {
        this.investorInfo = investorInfo;
    }

    public Long getId() {
        return id;
    }

    public UserCASDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public CasTypeEnum getCasTypeEnum() {
        return casTypeEnum;
    }

    public UserCASDetails setCasTypeEnum(CasTypeEnum casTypeEnum) {
        this.casTypeEnum = casTypeEnum;
        return this;
    }

    public FileTypeEnum getFileTypeEnum() {
        return fileTypeEnum;
    }

    public UserCASDetails setFileTypeEnum(FileTypeEnum fileTypeEnum) {
        this.fileTypeEnum = fileTypeEnum;
        return this;
    }

    public List<UserFolioDetails> getFolioEntities() {
        return folios;
    }

    public UserCASDetails setFolioEntities(List<UserFolioDetails> folios) {
        this.folios = folios;
        return this;
    }

    public InvestorInfo getInvestorInfoEntity() {
        return investorInfo;
    }

    public UserCASDetails setInvestorInfoEntity(InvestorInfo investorInfo) {
        if (investorInfo == null) {
            if (this.investorInfo != null) {
                this.investorInfo.setUserCasDetails(null);
            }
        } else {
            investorInfo.setUserCasDetails(this);
        }
        this.investorInfo = investorInfo;
        return this;
    }

    public void addFolioEntity(UserFolioDetails userFolioDetailsEntity) {
        this.folios.add(userFolioDetailsEntity);
        userFolioDetailsEntity.setUserCasDetails(this);
    }
}
