package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.*;
import static com.app.folioman.archunit.CommonRules.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceRulesTest {

    // Classes
    @ArchTest
    static final ArchRule component_annotation_is_not_allowed = classes()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .haveSimpleNameNotEndingWith("MapperImpl")
            .should()
            .notBeAnnotatedWith(Component.class)
            .because("Component annotation is not allowed in %s (except for generated mappers)"
                    .formatted(DOMAIN_PACKAGE));

    @ArchTest
    static final ArchRule service_implementations_should_be_annotated = classes()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .haveSimpleNameEndingWith("Impl")
            .and()
            .haveSimpleNameNotEndingWith("MapperImpl")
            .should()
            .beAnnotatedWith(Service.class)
            .because(ANNOTATED_EXPLANATION.formatted(SERVICE_SUFFIX, "@Service"));

    // Fields
    @ArchTest
    static final ArchRule fields_should_not_be_public = fields().that()
            .areDeclaredInClassesThat()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .areDeclaredInClassesThat()
            .areNotEnums()
            .should()
            .notBePublic()
            .because("Public fields are not allowed in %s (except for Enums)".formatted(DOMAIN_PACKAGE));

    // Constructors
    @ArchTest
    static final ArchRule constructors_should_be_package_private = constructors()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .areDeclaredInClassesThat()
            .areNotMemberClasses()
            .and()
            .areDeclaredInClassesThat()
            .areNotAnnotatedWith("jakarta.persistence.Entity")
            .and()
            .areDeclaredInClassesThat()
            .areNotAnnotatedWith("jakarta.persistence.MappedSuperclass")
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameNotEndingWith("DTO")
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameNotEndingWith("Projection")
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameNotEndingWith("MapperImpl")
            .and()
            .areDeclaredInClassesThat()
            .areNotEnums()
            .and()
            .areDeclaredInClassesThat()
            .areNotRecords()
            .should()
            .bePackagePrivate()
            .because("Package Private constructors are preferred in %s for internal components"
                    .formatted(DOMAIN_PACKAGE));

    // Methods
    @ArchTest
    static final ArchRule bean_methods_are_not_allowed = beanMethodsAreNotAllowedRule(DOMAIN_PACKAGE);

    @ArchTest
    static final ArchRule static_methods_are_not_allowed = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .areDeclaredInClassesThat()
            .areNotMemberClasses()
            .and()
            .areDeclaredInClassesThat()
            .areNotEnums()
            .and()
            .areDeclaredInClassesThat()
            .areNotRecords()
            .and()
            .doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should()
            .notBeStatic()
            .because("Static methods are not allowed in %s (except for Enums and Records)".formatted(DOMAIN_PACKAGE));
}
