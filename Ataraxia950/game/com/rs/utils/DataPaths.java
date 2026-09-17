package com.rs.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Single authority for the on-disk {@code data/} root that the JSON parsers,
 * the HCIM news lists and the mailing credentials read.
 *
 * <p>Before this class every parser hard-coded {@code "data/..."} and relied on
 * {@code new FileReader(String)} resolving it against the JVM working
 * directory. That was fine for the legacy 910 launcher (always started in the
 * server directory) but the native 947 JVM is OpenNXT, started with the
 * working directory {@code OpenNXT/}, so the staged {@code Ataraxia947/data}
 * tree was unreachable and the 947 bootstrap had to fail closed unless
 * {@code cwd == Ataraxia947}. The root is now resolved once per call in this
 * order:
 *
 * <ol>
 *   <li>an explicitly installed root ({@link #install(Path)}; the bootstrap
 *       installs the root it was given so parsers and bootstrap agree);</li>
 *   <li>the {@value #PROPERTY} system property, made absolute; it must be a
 *       directory, a bad value fails closed instead of silently falling back;</li>
 *   <li>{@code <working directory>/data} when it contains
 *       {@value #MARKER_FILE} (the legacy launcher path: identical behaviour
 *       to the old literals because {@code <cwd>/data/x} is the same file
 *       that {@code "data/x"} resolved to);</li>
 *   <li>otherwise an {@link IllegalStateException} naming both candidates and
 *       the property to set.</li>
 * </ol>
 *
 * <p>Nothing here is cached: the checks are a couple of stat calls and the
 * tests flip the property between cases. Nothing here reads a file either;
 * a resolved path is only a location, the caller still decides what a
 * missing file means.
 */
public final class DataPaths {

    /** System property naming the data root (absolute path preferred; relative values are made absolute). */
    public static final String PROPERTY = "ataraxia950.data";

    /**
     * The pre-port spelling, still honoured when {@link #PROPERTY} is unset.
     *
     * Nothing in this project passes it, but a rename that quietly stops honouring an existing -D
     * flag fails by ignoring the operator rather than by telling them, which is the worst way for
     * a path setting to be wrong.
     */
    public static final String LEGACY_PROPERTY = "ataraxia947.data";

    /** {@link #PROPERTY} if set, else {@link #LEGACY_PROPERTY}, else null. */
    private static String configuredRoot() {
        String value = System.getProperty(PROPERTY);
        if (value != null && !value.trim().isEmpty()) return value;
        return System.getProperty(LEGACY_PROPERTY);
    }

    /** File that marks a directory as a staged data root; the legacy cwd fallback is only taken when it exists. */
    public static final String MARKER_FILE = "npcs/spawns.json";

    /** Name of the legacy directory under the working directory. */
    static final String LEGACY_DIRECTORY = "data";

    private static volatile Path installed;

    private DataPaths() { }

    /**
     * Installs an explicit root that wins over the property and the working
     * directory until {@link #reset()}. Used by {@code Native950Bootstrap.run(Path)}
     * so the parsers read the same tree the bootstrap validated, and by tests.
     *
     * @throws IllegalArgumentException when {@code root} is not a directory
     */
    public static void install(Path root) {
        Objects.requireNonNull(root, "root");
        Path absolute = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(absolute))
            throw new IllegalArgumentException("Data root " + absolute + " is not a directory");
        installed = absolute;
    }

    /** Removes an installed root; resolution falls back to the property / working directory order. */
    public static void reset() { installed = null; }

    /** The installed root, or null when none is installed. */
    public static Path installed() { return installed; }

    /**
     * The resolved data root (absolute, normalised).
     *
     * @throws IllegalStateException when no candidate is usable (see the class javadoc)
     */
    public static Path root() {
        Path override = installed;
        if (override != null) return override;
        return resolveRoot(configuredRoot(), Paths.get("").toAbsolutePath());
    }

    /**
     * Pure resolution used by {@link #root()} and by the unit tests: the
     * property value (may be null or blank) and the working directory.
     *
     * @throws IllegalStateException when neither candidate is usable
     */
    public static Path resolveRoot(String propertyValue, Path workingDirectory) {
        Objects.requireNonNull(workingDirectory, "workingDirectory");
        Path legacy = workingDirectory.resolve(LEGACY_DIRECTORY).toAbsolutePath().normalize();
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            Path fromProperty = Paths.get(propertyValue.trim()).toAbsolutePath().normalize();
            if (!Files.isDirectory(fromProperty))
                throw new IllegalStateException("Data root from -D" + PROPERTY + "=" + propertyValue.trim()
                        + " is not a directory (" + fromProperty + "); the working-directory fallback " + legacy
                        + " is not consulted while the property is set");
            return fromProperty;
        }
        if (Files.isRegularFile(legacy.resolve(MARKER_FILE)))
            return legacy;
        throw new IllegalStateException("No data root: -D" + PROPERTY + " is not set and " + legacy
                + (Files.isDirectory(legacy) ? " does not contain " + MARKER_FILE : " does not exist")
                + "; start the JVM with -D" + PROPERTY + "=<absolute path to the staged data directory>"
                + " (e.g. <workspace>" + File.separator + "Ataraxia947" + File.separator + "data) or from the directory that contains "
                + LEGACY_DIRECTORY + File.separator + MARKER_FILE.replace('/', File.separatorChar));
    }

    /** Human-readable description of how {@link #root()} was resolved, for reports; never throws. */
    public static String describeResolution() {
        Path override = installed;
        if (override != null) return "installed: " + override;
        String property = configuredRoot();
        if (property != null && !property.trim().isEmpty()) return "-D" + PROPERTY + "=" + property.trim();
        Path legacy = Paths.get("").toAbsolutePath().resolve(LEGACY_DIRECTORY).normalize();
        if (Files.isRegularFile(legacy.resolve(MARKER_FILE))) return "working directory: " + legacy;
        return "unresolved (no -D" + PROPERTY + ", no " + legacy + File.separator + MARKER_FILE + ")";
    }

    /**
     * Absolute path of a file under the root. {@code relative} uses forward
     * slashes ({@code "items/shops.json"}); an absolute argument is returned
     * as-is so callers that already hold a full path are not re-rooted.
     */
    public static Path path(String relative) {
        Objects.requireNonNull(relative, "relative");
        Path candidate = Paths.get(relative);
        if (candidate.isAbsolute()) return candidate.normalize();
        return root().resolve(candidate).normalize();
    }

    /** {@link #path(String)} as a {@link File}. */
    public static File file(String relative) { return path(relative).toFile(); }

    /** {@link #path(String)} as a String for the {@code FileReader}-based parsers. */
    public static String resolve(String relative) { return path(relative).toString(); }
}
