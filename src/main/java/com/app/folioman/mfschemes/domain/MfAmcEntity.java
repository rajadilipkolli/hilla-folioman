package com.app.folioman.mfschemes.domain;

import com.app.folioman.shared.BaseEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Table(
        name = "mf_amc",
        schema = "mfschemes",
        indexes = {@Index(name = "idx_mf_amc_name_vector", columnList = "name_vector")})
@Entity
public class MfAmcEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mf_amc_gen")
    @SequenceGenerator(name = "mf_amc_gen", sequenceName = "mf_amc_seq", allocationSize = 1, schema = "mfschemes")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private String code;

    @OneToMany(mappedBy = "amc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfFundSchemeEntity> mfFundSchemes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public MfAmcEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MfAmcEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MfAmcEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCode() {
        return code;
    }

    public MfAmcEntity setCode(String code) {
        this.code = code;
        return this;
    }

    public List<MfFundSchemeEntity> getMfFundSchemes() {
        return mfFundSchemes;
    }

    public MfAmcEntity setMfFundSchemes(List<MfFundSchemeEntity> mfFundSchemes) {
        this.mfFundSchemes = mfFundSchemes;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MfAmcEntity.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("code='" + code + "'")
                .toString();
    }
}
