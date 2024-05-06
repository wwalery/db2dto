package dev.walgo.db2dto.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Table generator configuration.
 */
public class TableConfig {

    /**
     * Prefix for generated class name.
     *
     * <p>
     * empty by default
     */
    String classPrefix;

    /**
     * Suffix for generated class name.
     *
     * <p>
     * 'Data' by default
     */
    String classSuffix;

    /**
     * Java package name for generated class.
     *
     * <p>
     * 'dto' by default
     */
    String packageName;

    /** Order of generated column. */
    public ColumnOrder columnsOrder;

    /** Use default column value from DB */
    public Boolean useDefaults;

    /** List of read-only fields, common for all tables. */
    Set<String> readOnlyFields = new TreeSet<>();

    /** List of interfaces, for add to generated class. */
    Set<String> interfaces = new TreeSet<>();

    /** List of additional fields, for add to generated class. */
    Map<String, String> additionalFields = new HashMap<>();

    /** Consider field as enumerate, for generated class. */
    Map<String, String> enumFields = new HashMap<>();

    /** Ignore this fields in 'toString' method by table. */
    Set<String> toStringIgnoreFields = new TreeSet<>();

    /** Set field type forcibly. */
    Map<String, String> fieldTypes = new HashMap<>();

    /** Rename field. */
    Map<String, String> fieldNames = new HashMap<>();

    /** Field default value (for ...NotNull methods). */
    Map<String, String> fieldDefaults = new HashMap<>();

    /** Type default value (for ...NotNull methods). */
    Map<String, String> typeDefaults = new HashMap<>();

    /**
     * Column order type
     */
    public enum ColumnOrder {
        /**
         * Sorted in alphabet order
         */
        ALPHA,
        /**
         * Order as in table
         */
        TABLE
    }

}
