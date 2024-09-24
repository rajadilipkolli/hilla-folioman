package com.app.folioman.portfolio.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "schemevalue", schema = "portfolio")
public class SchemeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_value_gen")
    @SequenceGenerator(name = "scheme_value_gen", sequenceName = "scheme_value_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate date;

    @Column(precision = 30, scale = 2)
    private BigDecimal invested;

    @Column(precision = 30, scale = 2)
    private BigDecimal value;

    @Column(precision = 30, scale = 10)
    private BigDecimal avgNav;

    @Column(precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(precision = 20, scale = 3)
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id", nullable = false)
    private UserSchemeDetails userSchemeDetails;

    public Long getId() {
        return id;
    }

    public SchemeValue setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public SchemeValue setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public SchemeValue setInvested(BigDecimal invested) {
        this.invested = invested;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public SchemeValue setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public BigDecimal getAvgNav() {
        return avgNav;
    }

    public SchemeValue setAvgNav(BigDecimal avgNav) {
        this.avgNav = avgNav;
        return this;
    }

    public BigDecimal getNav() {
        return nav;
    }

    public SchemeValue setNav(BigDecimal nav) {
        this.nav = nav;
        return this;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public SchemeValue setBalance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public UserSchemeDetails getUserSchemeDetails() {
        return userSchemeDetails;
    }

    public SchemeValue setUserSchemeDetails(UserSchemeDetails userSchemeDetails) {
        this.userSchemeDetails = userSchemeDetails;
        return this;
    }
}
