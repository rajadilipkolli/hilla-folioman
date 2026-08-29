package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.app.folioman.shared.BaseEntity;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

@AnalyzeClasses(packages = DEFAULT_PACKAGE)
class EntityRulesTest {

    @ArchTest
    static final ArchRule entities_should_extend_base_entity = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .beAssignableTo(BaseEntity.class)
            .because("All JPA entities should extend BaseEntity for JPA auditing");
}
