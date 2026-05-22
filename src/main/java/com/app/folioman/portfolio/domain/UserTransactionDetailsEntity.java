package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        name = "user_transaction_details",
        schema = "portfolio",
        indexes = {@Index(name = "user_details_idx_type_transaction_dat", columnList = "transaction_date, type")})
@SuppressWarnings("NullAway.Init")
class UserTransactionDetailsEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_transaction_details_seq")
    @SequenceGenerator(name = "user_transaction_details_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    private @Nullable LocalDate transactionDate;
    private @Nullable String description;
    private @Nullable BigDecimal amount;
    private @Nullable Double units;
    private @Nullable Double nav;
    private @Nullable Double balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    private @Nullable String dividendRate;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id")
    private @Nullable UserSchemeDetailsEntity userSchemeDetails;

    public Long getId() {
        return id;
    }

    public UserTransactionDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public @Nullable LocalDate getTransactionDate() {
        return transactionDate;
    }

    public UserTransactionDetailsEntity setTransactionDate(@Nullable LocalDate transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public UserTransactionDetailsEntity setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    public @Nullable BigDecimal getAmount() {
        return amount;
    }

    public UserTransactionDetailsEntity setAmount(@Nullable BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public @Nullable Double getUnits() {
        return units;
    }

    public UserTransactionDetailsEntity setUnits(Double units) {
        this.units = units;
        return this;
    }

    public @Nullable Double getNav() {
        return nav;
    }

    public UserTransactionDetailsEntity setNav(@Nullable Double nav) {
        this.nav = nav;
        return this;
    }

    public @Nullable Double getBalance() {
        return balance;
    }

    public UserTransactionDetailsEntity setBalance(@Nullable Double balance) {
        this.balance = balance;
        return this;
    }

    public TransactionType getType() {
        return type;
    }

    public UserTransactionDetailsEntity setType(TransactionType type) {
        this.type = type;
        return this;
    }

    public @Nullable String getDividendRate() {
        return dividendRate;
    }

    public UserTransactionDetailsEntity setDividendRate(@Nullable String dividendRate) {
        this.dividendRate = dividendRate;
        return this;
    }

    public @Nullable UserSchemeDetailsEntity getUserSchemeDetails() {
        return userSchemeDetails;
    }

    public UserTransactionDetailsEntity setUserSchemeDetails(@Nullable UserSchemeDetailsEntity userSchemeDetails) {
        this.userSchemeDetails = userSchemeDetails;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserTransactionDetailsEntity that = (UserTransactionDetailsEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
