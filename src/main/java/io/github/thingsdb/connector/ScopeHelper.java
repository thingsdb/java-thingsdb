package io.github.thingsdb.connector;

import java.util.regex.Pattern;

public final class ScopeHelper {

    private static final Pattern VALID_NAME_REGEX =
        Pattern.compile("^[A-Za-z_][0-9A-Za-z_]{0,254}$");

    private ScopeHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isName(String s) {
        return s != null && !s.isEmpty() && VALID_NAME_REGEX.matcher(s).matches();
    }

    public static String cnScope(String scope) {
        if (scope == null) {
            throw new IllegalArgumentException("invalid (collection) scope name: null");
        }

        String name = "";

        // Compatible fallback: Standard string checks and index tracking
        if (scope.contains(":")) {
            String[] parts = scope.split(":");
            name = parts[parts.length - 1];
        } else if (scope.contains("/")) {
            String[] parts = scope.split("/");
            name = parts[parts.length - 1];
        }

        if (isName(name)) {
            return name;
        }

        throw new IllegalArgumentException("invalid (collection) scope name: " + scope);
    }

    public static String fcScope(String scope) {
        if (scope != null && scope.startsWith("@collection:")) {
            return scope;
        }

        String cn = cnScope(scope);
        return "@collection:" + cn;
    }
}