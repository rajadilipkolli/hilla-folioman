/* Licensed under Apache-2.0 2021-2024. */
package com.example.application.entities;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<T> {

    @CreatedBy
    protected T createdBy;

    @CreatedDate
    protected LocalDateTime createdDate;

    @LastModifiedBy
    protected T lastModifiedBy;

    @LastModifiedDate
    protected LocalDateTime lastModifiedDate;

    public T getCreatedBy() {
        return createdBy;
    }

    public Auditable<T> setCreatedBy(T createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Auditable<T> setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public T getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Auditable<T> setLastModifiedBy(T lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Auditable<T> setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }
}
