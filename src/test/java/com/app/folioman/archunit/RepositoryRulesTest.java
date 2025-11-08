package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.*;
import static com.app.folioman.archunit.CommonRules.interfacesAreOnlyAllowedRule;
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
    static final ArchRule classes_should_be_annotated = classes()
            .that()
            .resideInAPackage(REPOSITORY_PACKAGE)
            .should()
            .beAnnotatedWith(Repository.class)
            .because(ANNOTATED_EXPLANATION.formatted(REPOSITORY_SUFFIX, "@Repository"));

    @ArchTest
    static final ArchRule classesShouldBeInterfaces = interfacesAreOnlyAllowedRule(REPOSITORY_PACKAGE);
}
