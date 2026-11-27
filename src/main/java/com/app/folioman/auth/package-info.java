/**
 * The Authentication module handles user authentication, JWT tokens, and security contexts.
 */
@ApplicationModule(
        displayName = "Authentication",
        allowedDependencies = {"portfolio"})
@NullMarked
package com.app.folioman.auth;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
