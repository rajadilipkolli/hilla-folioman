package com.app.folioman.portfolio.entities;

import com.app.folioman.shared.Auditable;
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

    public List<UserFolioDetails> getFolios() {
        return folios;
    }

    public UserCASDetails setFolios(List<UserFolioDetails> folios) {
        this.folios = folios;
        return this;
    }

    public InvestorInfo getInvestorInfo() {
        return investorInfo;
    }

    public UserCASDetails setInvestorInfo(InvestorInfo investorInfo) {
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
