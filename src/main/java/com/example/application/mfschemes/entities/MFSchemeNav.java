package com.example.application.mfschemes.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Table(
        name = "mf_scheme_nav",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_mf_scheme_nav",
                    columnNames = {"nav", "nav_date", "mf_scheme_id"})
        })
@Entity
public class MFSchemeNav extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(precision = 10, scale = 4)
    private BigDecimal nav;

    @Column(name = "nav_date")
    private LocalDate navDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_id")
    private MFScheme mfSchemeEntity;

    public Long getId() {
        return id;
    }

    public MFSchemeNav setId(Long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getNav() {
        return nav;
    }

    public MFSchemeNav setNav(BigDecimal nav) {
        this.nav = nav;
        return this;
    }

    public LocalDate getNavDate() {
        return navDate;
    }

    public MFSchemeNav setNavDate(LocalDate navDate) {
        this.navDate = navDate;
        return this;
    }

    public MFScheme getMfSchemeEntity() {
        return mfSchemeEntity;
    }

    public MFSchemeNav setMfSchemeEntity(MFScheme mfSchemeEntity) {
        this.mfSchemeEntity = mfSchemeEntity;
        return this;
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
        MFSchemeNav that = (MFSchemeNav) o;
        return Objects.equals(getNav(), that.getNav())
                && Objects.equals(
                        getMfSchemeEntity().getSchemeId(),
                        that.getMfSchemeEntity().getSchemeId())
                && Objects.deepEquals(getNavDate(), that.getNavDate());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
