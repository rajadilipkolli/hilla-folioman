package com.app.folioman.portfolio.entities;

import com.app.folioman.portfolio.models.request.TransactionType;
import com.app.folioman.shared.Auditable;
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
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(
        name = "user_transaction_details",
        schema = "portfolio",
        indexes = {@Index(name = "user_details_idx_type_transaction_dat", columnList = "transaction_date, type")})
public class UserTransactionDetails extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_transaction_details_seq")
    @SequenceGenerator(name = "user_transaction_details_seq", schema = "portfolio")
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate transactionDate;
    private String description;
    private BigDecimal amount;
    private Double units;
    private Double nav;
    private Double balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    private String dividendRate;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id")
    private UserSchemeDetails userSchemeDetails;

    public Long getId() {
        return id;
    }

    public UserTransactionDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public UserTransactionDetails setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UserTransactionDetails setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UserTransactionDetails setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Double getUnits() {
        return units;
    }

    public UserTransactionDetails setUnits(Double units) {
        this.units = units;
        return this;
    }

    public Double getNav() {
        return nav;
    }

    public UserTransactionDetails setNav(Double nav) {
        this.nav = nav;
        return this;
    }

    public Double getBalance() {
        return balance;
    }

    public UserTransactionDetails setBalance(Double balance) {
        this.balance = balance;
        return this;
    }

    public TransactionType getType() {
        return type;
    }

    public UserTransactionDetails setType(TransactionType type) {
        this.type = type;
        return this;
    }

    public String getDividendRate() {
        return dividendRate;
    }

    public UserTransactionDetails setDividendRate(String dividendRate) {
        this.dividendRate = dividendRate;
        return this;
    }

    public UserSchemeDetails getUserSchemeDetails() {
        return userSchemeDetails;
    }

    public UserTransactionDetails setUserSchemeDetails(UserSchemeDetails userSchemeDetails) {
        this.userSchemeDetails = userSchemeDetails;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserTransactionDetails that = (UserTransactionDetails) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
