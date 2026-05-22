package com.app.folioman.mfschemes.domain;

import com.app.folioman.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        name = "mf_scheme_types",
        schema = "mfschemes",
        uniqueConstraints =
                @UniqueConstraint(
                        columnNames = {"type", "category", "sub_category"},
                        name = "UK_MF_SCHEME_CATEGORY_MF_SCHEME_TYPE"))
@SuppressWarnings("NullAway.Init")
class MFSchemeTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_type_id_generator")
    @SequenceGenerator(
            name = "scheme_type_id_generator",
            sequenceName = "mf_scheme_types_seq",
            allocationSize = 2,
            schema = "mfschemes")
    @Column(name = "scheme_type_id", nullable = false)
    private Integer schemeTypeId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sub_category")
    private @Nullable String subCategory;

    @OneToMany(mappedBy = "mfSchemeTypeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfFundSchemeEntity> mfSchemes = new ArrayList<>();

    public Integer getSchemeTypeId() {
        return schemeTypeId;
    }

    public MFSchemeTypeEntity setSchemeTypeId(Integer schemeTypeId) {
        this.schemeTypeId = schemeTypeId;
        return this;
    }

    public String getType() {
        return type;
    }

    public MFSchemeTypeEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public MFSchemeTypeEntity setCategory(String category) {
        this.category = category;
        return this;
    }

    public @Nullable String getSubCategory() {
        return subCategory;
    }

    public MFSchemeTypeEntity setSubCategory(@Nullable String subCategory) {
        this.subCategory = subCategory;
        return this;
    }

    public List<MfFundSchemeEntity> getMfSchemes() {
        return mfSchemes;
    }

    public MFSchemeTypeEntity setMfSchemes(List<MfFundSchemeEntity> mfSchemeEntities) {
        this.mfSchemes = mfSchemeEntities;
        return this;
    }

    public void addMFScheme(MfFundSchemeEntity mfScheme) {
        mfSchemes.add(mfScheme);
        mfScheme.setMfSchemeTypeEntity(this);
    }

    @Override
    public String toString() {
        return "MFSchemeTypeEntity [schemeTypeId=" + schemeTypeId + ", type=" + type + ", category=" + category
                + ", subCategory=" + subCategory + ", version=" + version + "]";
    }
}
