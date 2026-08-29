package com.app.folioman.archunit;

import static com.app.folioman.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.app.folioman.archunit.CommonRules.notCallLenientMethod;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

@AnalyzeClasses(
        packages = DEFAULT_PACKAGE,
        importOptions = {ImportOption.OnlyIncludeTests.class})
class TestCodingRulesTest {

    @ArchTest
    static final ArchRule noLenientUsageInTests = noClasses()
            .that()
            .haveSimpleNameEndingWith("Test")
            .or()
            .haveSimpleNameEndingWith("Tests")
            .or()
            .areAnnotatedWith(Test.class)
            .should(notCallLenientMethod())
            .because("lenient() should not be used in tests as it can hide unused stubs and make tests less reliable");
}
