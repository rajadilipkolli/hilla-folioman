@ApplicationModule(
        displayName = "Application Configuration",
        allowedDependencies = {"mfschemes", "portfolio", "portfolio::exception", "shared"})
@NullMarked
package com.app.folioman.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
