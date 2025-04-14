package com.app.folioman.mfschemes.entities;

import com.app.folioman.shared.Auditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Table(
        name = "mf_amc",
        schema = "mfschemes",
        indexes = {@Index(name = "idx_mf_amc_name_vector", columnList = "name_vector")})
@Entity
public class MfAmc extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mf_amc_gen")
    @SequenceGenerator(name = "mf_amc_gen", sequenceName = "mf_amc_seq", allocationSize = 1, schema = "mfschemes")
    @Column(name = "id", nullable = false)
    private Integer id;

    private String name;

    private String description;

    @Column(nullable = false)
    private String code;

    @Column(name = "name_vector", insertable = false, updatable = false)
    private String nameVector;

    @OneToMany(mappedBy = "amc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfFundScheme> mfFundSchemes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public MfAmc setId(Integer id) {
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

    public String getNameVector() {
        return nameVector;
    }

    public MfAmc setNameVector(String nameVector) {
        this.nameVector = nameVector;
        return this;
    }

    public List<MfFundScheme> getMfFundSchemes() {
        return mfFundSchemes;
    }

    public MfAmc setMfFundSchemes(List<MfFundScheme> mfFundSchemes) {
        this.mfFundSchemes = mfFundSchemes;
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
