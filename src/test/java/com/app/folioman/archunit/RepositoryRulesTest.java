package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryRulesTest {

    // Classes
    @ArchTest
    static final ArchRule repositories_should_be_annotated = classes()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .and()
            .doNotHaveSimpleName("package-info")
            .should()
            .beAnnotatedWith(Repository.class)
            .because(ANNOTATED_EXPLANATION.formatted(REPOSITORY_SUFFIX, "@Repository"));

    @ArchTest
    static final ArchRule repositories_should_be_interfaces = classes()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .and()
            .haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .should()
            .beInterfaces()
            .because("Repositories should be interfaces in %s".formatted(DOMAIN_PACKAGE));
}
