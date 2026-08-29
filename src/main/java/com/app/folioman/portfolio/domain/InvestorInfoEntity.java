package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "investor_info", schema = "portfolio")
@SuppressWarnings("NullAway.Init")
class InvestorInfoEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "investor_info_seq")
    @SequenceGenerator(name = "investor_info_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "address")
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_cas_details_id")
    private @Nullable UserCasDetailsEntity userCasDetailsEntity;

    public Long getId() {
        return id;
    }

    public InvestorInfoEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public InvestorInfoEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public InvestorInfoEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public InvestorInfoEntity setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public InvestorInfoEntity setAddress(String address) {
        this.address = address;
        return this;
    }

    public @Nullable UserCasDetailsEntity getUserCasDetailsEntity() {
        return userCasDetailsEntity;
    }

    public InvestorInfoEntity setUserCasDetailsEntity(
            @org.jspecify.annotations.Nullable UserCasDetailsEntity userCasDetailsEntity) {
        this.userCasDetailsEntity = userCasDetailsEntity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InvestorInfoEntity that = (InvestorInfoEntity) o;
        return id != null && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
