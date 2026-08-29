package com.app.folioman.shared;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    protected @Nullable Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    protected @Nullable Instant updatedAt;

    @Version
    protected @Nullable Short version;

    public @Nullable Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@Nullable Instant createdAt) {
        this.createdAt = createdAt;
    }

    public @Nullable Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@Nullable Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @Nullable Short getVersion() {
        return version;
    }

    public void setVersion(@Nullable Short version) {
        this.version = version;
    }
}
