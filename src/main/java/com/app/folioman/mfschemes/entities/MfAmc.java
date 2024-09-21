package com.app.folioman.mfschemes.entities;

import com.app.folioman.shared.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.StringJoiner;

@Table(name = "mf_amc", schema = "mfschemes")
@Entity
public class MfAmc extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mf_amc_gen")
    @SequenceGenerator(name = "mf_amc_gen", sequenceName = "mf_amc_seq", allocationSize = 5, schema = "mfschemes")
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private String description;

    @Column(nullable = false)
    private String code;

    public Long getId() {
        return id;
    }

    public MfAmc setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MfAmc setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MfAmc setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCode() {
        return code;
    }

    public MfAmc setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MfAmc.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("code='" + code + "'")
                .toString();
    }
}
