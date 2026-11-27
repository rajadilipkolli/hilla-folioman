package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.app.folioman.shared.BaseEntity;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

@AnalyzeClasses(packages = DEFAULT_PACKAGE)
class EntityRulesTest {

    @ArchTest
    static final ArchRule entities_should_extend_base_entity = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .beAssignableTo(BaseEntity.class)
            .because("All JPA entities should extend BaseEntity for JPA auditing");

    @ArchTest
    static final ArchRule entities_should_not_use_date_or_timestamp = ArchRuleDefinition.fields()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Entity.class)
            .should(CustomConditions.notUseDateOrTimestamp())
            .because("We should use modern Date/Time API like Instant or LocalDate");

    @ArchTest
    static final ArchRule enums_should_use_enum_type_string = ArchRuleDefinition.fields()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Entity.class)
            .and()
            .areAnnotatedWith(Enumerated.class)
            .should(CustomConditions.useEnumTypeString())
            .because("Enums should be stored as string in the database");
}
