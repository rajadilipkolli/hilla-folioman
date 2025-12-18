package com.app.folioman.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

/**
 * ImportOption to exclude the main Application class from ArchUnit analysis.
 * This allows specific deprecated usages on the Application class without failing the rules.
 */
public class ExcludeApplicationImportOption implements ImportOption {

    @Override
    public boolean includes(Location location) {
        String path = location.toString().replace('\\', '/');
        // Exclude the Application.class bytecode from analysis
        return !path.contains("/com/app/folioman/Application.class");
    }
}
