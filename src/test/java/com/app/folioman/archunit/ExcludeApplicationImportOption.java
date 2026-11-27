package com.app.folioman.archunit;

import com.app.folioman.Application;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

/**
 * ImportOption to exclude the main Application class from ArchUnit analysis.
 * This allows specific deprecated usages on the Application class without failing the rules.
 */
public class ExcludeApplicationImportOption implements ImportOption {

    @Override
    public boolean includes(Location location) {
        // Exclude the specific Application class by its canonical name
        return !location.contains(Application.class.getName().replace('.', '/'));
    }
}
