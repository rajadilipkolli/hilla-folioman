package com.app.folioman;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ApplicationIntTest extends AbstractIntegrationTest {

    private static ApplicationModules modules;

    @BeforeAll
    static void setUp() {
        modules = ApplicationModules.of(Application.class);
    }

    @Test
    @DisplayName("Application modules should be valid with no violations")
    void verifyModulesHaveNoViolations() {
        // This will throw an exception if there are any violations including:
        // - Cyclic dependencies between modules
        // - Invalid access to internal (non-exposed) types
        // - Missing module declarations
        modules.verify();
    }

    @Test
    @DisplayName("mfschemes module should exist and be properly structured")
    void verifyMfSchemesModuleStructure() {
        ApplicationModule mfSchemes = modules.getModuleByName("mfschemes")
                .orElseThrow(() -> new AssertionError("mfschemes module not found"));

        // Verify module has base packages
        assertThat(mfSchemes.getBasePackage().getName()).isEqualTo("com.app.folioman.mfschemes");

        // Verify module exposes public API (interfaces like MFNavService,
        // MfSchemeService)
        assertThat(mfSchemes.getNamedInterfaces()).isNotEmpty();
    }

    @Test
    @DisplayName("portfolio module should exist and be properly structured")
    void verifyPortfolioModuleStructure() {
        ApplicationModule portfolio = modules.getModuleByName("portfolio").orElseThrow();

        assertThat(portfolio.getBasePackage().getName()).isEqualTo("com.app.folioman.portfolio");
        assertThat(portfolio.getNamedInterfaces()).isNotEmpty();
    }

    @Test
    @DisplayName("shared module should exist")
    void verifySharedModuleExists() {
        ApplicationModule shared = modules.getModuleByName("shared").orElseThrow();

        assertThat(shared.getBasePackage().getName()).isEqualTo("com.app.folioman.shared");
    }

    @Test
    @DisplayName("All expected modules should be present")
    void verifyAllModulesPresent() {
        assertThat(modules.stream().map(m -> m.getIdentifier().toString()))
                .contains("mfschemes", "portfolio", "shared");
    }

    @Test
    void createModulithsDocumentation() {

        new Documenter(modules).writeDocumentation();
    }
}
