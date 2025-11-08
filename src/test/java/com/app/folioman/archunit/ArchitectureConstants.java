package com.app.folioman.archunit;

public class ArchitectureConstants {

    public static final String SERVICE_SUFFIX = "Service";

    // Packages
    public static final String ENTITY_PACKAGE = "..entities..";
    public static final String SERVICE_PACKAGE = "..service..";

    // Package to scan
    public static final String DEFAULT_PACKAGE = "com.app.folioman";

    // Explanations
    public static final String ANNOTATED_EXPLANATION = "Classes in %s package should be annotated with %s";

    private ArchitectureConstants() {}
}
