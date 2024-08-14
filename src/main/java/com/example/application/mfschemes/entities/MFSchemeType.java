/* Licensed under Apache-2.0 2021-2024. */
package com.example.application.mfschemes.entities;

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
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mf_scheme_types", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "category"}))
public class MFSchemeType extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_type_id_generator")
    @SequenceGenerator(name = "scheme_type_id_generator", sequenceName = "scheme_type_id_seq", allocationSize = 2)
    @Column(name = "scheme_type_id", nullable = false)
    private Integer schemeTypeId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Version
    private Short version;

    @OneToMany(mappedBy = "mfSchemeType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFScheme> mfSchemes = new ArrayList<>();

    public Integer getSchemeTypeId() {
        return schemeTypeId;
    }

    public MFSchemeType setSchemeTypeId(Integer schemeTypeId) {
        this.schemeTypeId = schemeTypeId;
        return this;
    }

    public String getType() {
        return type;
    }

    public MFSchemeType setType(String type) {
        this.type = type;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public MFSchemeType setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public MFSchemeType setSubCategory(String subCategory) {
        this.subCategory = subCategory;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public MFSchemeType setVersion(Short version) {
        this.version = version;
        return this;
    }

    public List<MFScheme> getMfSchemes() {
        return mfSchemes;
    }

    public MFSchemeType setMfSchemes(List<MFScheme> mfSchemeEntities) {
        this.mfSchemes = mfSchemeEntities;
        return this;
    }

    public void addMFScheme(MFScheme mfScheme) {
        mfSchemes.add(mfScheme);
        mfScheme.setMfSchemeType(this);
    }

    @Override
    public String toString() {
        return "MFSchemeTypeEntity [schemeTypeId=" + schemeTypeId + ", type=" + type + ", category=" + category
                + ", subCategory=" + subCategory + ", version=" + version + "]";
    }
}
