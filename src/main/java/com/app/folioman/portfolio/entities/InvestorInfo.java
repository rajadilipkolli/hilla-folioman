package com.app.folioman.portfolio.entities;

import com.app.folioman.shared.Auditable;
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
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "investor_info", schema = "portfolio")
public class InvestorInfo extends Auditable<String> implements Serializable {
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
    private UserCASDetails userCasDetails;

    public Long getId() {
        return id;
    }

    public InvestorInfo setId(Long id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public InvestorInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public InvestorInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public InvestorInfo setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public InvestorInfo setAddress(String address) {
        this.address = address;
        return this;
    }

    public UserCASDetails getUserCasDetails() {
        return userCasDetails;
    }

    public InvestorInfo setUserCasDetails(UserCASDetails userCASDetails) {
        this.userCasDetails = userCASDetails;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InvestorInfo that = (InvestorInfo) o;
        return id != null && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
