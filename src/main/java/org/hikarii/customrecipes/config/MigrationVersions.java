package org.hikarii.customrecipes.config;

public final class MigrationVersions {
    public static final int CONFIG_VERSION = 2;
    public static final int VANILLA_RECIPES_VERSION = 2;

    public static final String CONFIG_VERSION_KEY = "config-version";
    public static final String VANILLA_RECIPES_VERSION_KEY = "vanilla-recipes-version";

    private MigrationVersions() {
        throw new UnsupportedOperationException("Utility class");
    }
}