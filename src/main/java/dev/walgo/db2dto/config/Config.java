package dev.walgo.db2dto.config;

import com.google.common.base.Strings;
import dev.walgo.db2dto.DBColumn;
import dev.walgo.db2dto.DBTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Common configuration. */
public class Config {

    private static Config CONFIG;

    // DB config
    /** URL to database, required. */
    public String dbURL;
    /** Database user, required. */
    public String dbUser;
    /** Database password, optional. */
    public String dbPassword = "";
    /** Database schema, optional. */
    public String dbSchema;

    /**
     * Path to templates for source generation.
     *
     * <p>
     * './templates' by default
     */
    public String templateDir = "./templates";

    /**
     * Output directory for generated source.
     *
     * <p>
     * './build/dto/generated' by default
     */
    public String sourceOutputDir = "./build/dto/generated";

    /** compile generated source to jar. */
    public boolean compile;

    /**
     * Output directory for compiled classes.
     *
     * <p>
     * './build/dto/classes' by default
     */
    public String classOutputDir = "./build/dto/classes";

    /** Compiled jar name. */
    public String jarPath = "build/db2dto_generated.jar";

    /**
     * Name of base interface for all generated classes.
     */
    public String baseInterfaceName;

    /** Generate ARRAY column as List. Otherwise - array ([]). */
    public boolean arrayAsList;

    /** Plugin packages list * */
    public final List<String> pluginPackages;

    /** Map between SQL type name and java type. */
    public Map<String, String> sqlTypes = new HashMap<>();

    /** Common data for all tables. */
    public final TableConfig common;

    /** All tables. */
    public final Map<String, TableConfig> tables;

    /** Common constructor */
    public Config() {
        this.common = new TableConfig();
        this.tables = new HashMap<>();
        this.common.classPrefix = "";
        this.common.classSuffix = "Data";
        this.common.packageName = "dto";
        this.pluginPackages = new ArrayList<>();
        CONFIG = this;
        // this.common.readOnlyFields = new TreeSet<>();
        // this.common.interfaces = new HashSet<>();
        // this.common.additionalFields = new HashMap<>();
        // this.common.enumFields = new HashMap<>();
        // this.common.toStringIgnoreFields = new HashSet<>();
    }

    public static Config getCONFIG() {
        return CONFIG;
    }

    public void setClassPrefix(String classPrefixValue) {
        common.classPrefix = classPrefixValue;
    }

    /**
     * Prefix for class.
     *
     * @param tableName        table name
     * @param classPrefixValue prefix for class, generated for given table
     */
    public void setClassPrefix(String tableName, String classPrefixValue) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).classPrefix = classPrefixValue;
    }

    /**
     * Get prefix for class, generated for given table
     *
     * @param tableName table name
     * @return class prefix
     */
    public String getClassPrefix(String tableName) {
        TableConfig table = tables.get(tableName);
        if ((table != null) && (table.classPrefix != null)) {
            return table.classPrefix;
        } else {
            return common.classPrefix;
        }
    }

    /**
     * Suffix for class.
     *
     * @param classSuffixValue suffix value
     */
    public void setClassSuffix(String classSuffixValue) {
        common.classSuffix = classSuffixValue;
    }

    public void setClassSuffix(String tableName, String classSuffixValue) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).classSuffix = classSuffixValue;
    }

    public String getClassSuffix(String tableName) {
        TableConfig table = tables.get(tableName);
        if ((table != null) && (table.classSuffix != null)) {
            return table.classSuffix;
        } else {
            return common.classSuffix;
        }
    }

    public void setPackageName(String packageNameValue) {
        common.packageName = packageNameValue;
    }

    /**
     * Link package name to table name
     *
     * @param tableName        table name
     * @param packageNameValue package for given table
     */
    public void setPackageName(String tableName, String packageNameValue) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).packageName = packageNameValue;
    }

    /**
     * Gets package name by table name or default.
     *
     * @param tableName table name
     * @return package name for table
     */
    public String getPackageName(String tableName) {
        TableConfig table = tables.get(tableName);
        if ((table != null) && (table.packageName != null)) {
            return table.packageName;
        } else {
            return common.packageName;
        }
    }

    public void setFieldReadOnly(String fieldName) {
        common.readOnlyFields.add(fieldName);
    }

    public void setFieldReadOnly(String tableName, String fieldName) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).readOnlyFields.add(fieldName);
    }

    /**
     * Register default (common) interface, for add to generated class.
     *
     * @param interfaceClass interface class full name
     */
    public void useInterface(String interfaceClass) {
        common.interfaces.add(interfaceClass);
    }

    /**
     * Register interface by table name.
     *
     * @param tableName      table name
     * @param interfaceClass interface class for table
     */
    public void useInterface(String tableName, String interfaceClass) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).interfaces.add(interfaceClass);
    }

    /**
     * Gets interface list by table name or default.
     *
     * @param tableName table name
     * @return set of interfaces for table
     */
    public Set<String> getInterfaces(String tableName) {
        TableConfig table = tables.get(tableName);
        Set<String> interfaces = new TreeSet<>(common.interfaces);
        if (table != null) {
            interfaces.addAll(table.interfaces);
        }
        return interfaces;
    }

    /**
     * Add field, for add to all generated class.
     *
     * @param fieldName field name
     * @param fieldType type for that field
     */
    public void addField(String fieldName, String fieldType) {
        common.additionalFields.put(fieldName, fieldType);
    }

    /**
     * Additional field (not existing in table) by table name.
     *
     * @param tableName table name
     * @param fieldName field name in table
     * @param fieldType type for that field
     */
    public void addField(String tableName, String fieldName, String fieldType) {
        tables
                .computeIfAbsent(tableName, key -> new TableConfig()).additionalFields
                .put(fieldName, fieldType);
    }

    /**
     * Gets additional fields list by table name or default.
     *
     * @param tableName table name
     * @return fields in table
     */
    public List<DBColumn> getFields(String tableName) {
        TableConfig table = tables.get(tableName);
        List<DBColumn> fields = new ArrayList<>(
                common.additionalFields.entrySet()
                        .stream()
                        .map(it -> {
                            DBColumn column = new DBColumn(it.getKey(), it.getValue());
                            column.tableName = tableName;
                            return column;
                        })
                        .collect(Collectors.toList()));
        if (table != null) {
            fields.addAll(
                    table.additionalFields.entrySet()
                            .stream()
                            .map(it -> {
                                DBColumn column = new DBColumn(it.getKey(), it.getValue());
                                column.tableName = tableName;
                                return column;
                            })
                            .collect(Collectors.toList()));
        }
        return fields;
    }

    /**
     * Consider field as enumerate, for generated classes for all tables.
     *
     * @param fieldName field name
     * @param enumType  enum class
     */
    public void asEnum(String fieldName, String enumType) {
        common.enumFields.put(fieldName, enumType);
    }

    /**
     * Consider field as enumerate by table name.
     *
     * @param tableName table name
     * @param fieldName field name in table
     * @param enumType  enum class for field
     */
    public void asEnum(String tableName, String fieldName, String enumType) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).enumFields.put(fieldName, enumType);
    }

    /**
     * Gets fields considered as enumerate.
     *
     * @param tableName table name
     * @return set of enums for table
     */
    public Map<String, String> getEnums(String tableName) {
        TableConfig table = tables.get(tableName);
        Map<String, String> enums = new HashMap<>(common.enumFields);
        if (table != null) {
            enums.putAll(table.enumFields);
        }
        return enums;
    }

    public boolean isEnum(final String tableName, final String fieldName) {
        return getEnums(tableName).containsKey(fieldName);
    }

    public String getEnum(final String tableName, final String fieldName) {
        return getEnums(tableName).get(fieldName);
    }

    /** Ignore this fields in 'toString' method for all tables. */
    /**
     * Add field, for add to all generated class.
     *
     * @param fieldName field name
     */
    public void addToStringIgnore(String fieldName) {
        common.toStringIgnoreFields.add(fieldName);
    }

    /**
     * Gets list of fields, ignored in 'toString' method by table.
     *
     * @param tableName table name
     * @param fieldName field name
     */
    public void addToStringIgnore(String tableName, String fieldName) {
        tables.computeIfAbsent(tableName, key -> new TableConfig()).toStringIgnoreFields.add(fieldName);
    }

    /**
     * Gets additional fields list by table name or default.
     *
     * @param tableName table name
     * @return ignored fields
     */
    public Set<String> getToStringIgnoredFields(String tableName) {
        TableConfig table = tables.get(tableName);
        Set<String> fields = new TreeSet<>(common.toStringIgnoreFields);
        if (table != null) {
            fields.addAll(table.toStringIgnoreFields);
        }
        return fields;
    }

    public List<DBColumn> getToStringFields(DBTable table) {
        Set<String> ignoredFields = getToStringIgnoredFields(table.name);
        List<DBColumn> result = new ArrayList<>(
                table.columns.stream()
                        .filter(it -> !ignoredFields.contains(it.name))
                        .collect(Collectors.toList()));
        result.addAll(
                getFields(table.name).stream()
                        .filter(it -> !ignoredFields.contains(it.name))
                        .collect(Collectors.toList()));
        return result;
    }

    public boolean isReadOnlyField(final String tableName, final String fieldName) {
        if (common.readOnlyFields.contains(fieldName)) {
            return true;
        }
        TableConfig table = tables.get(tableName);
        if (table == null) {
            return false;
        }
        return table.readOnlyFields.contains(fieldName);
    }

    public boolean isSyntheticField(final String tableName, final String fieldName) {
        if (common.additionalFields.containsKey(fieldName)) {
            return true;
        }
        TableConfig table = tables.get(tableName);
        if (table == null) {
            return false;
        }
        return table.additionalFields.containsKey(fieldName);
    }

    /**
     * Set field type forcibly for all tables.
     *
     * @param fieldName field name
     * @param fieldType field type
     */
    public void asFieldType(String fieldName, String fieldType) {
        common.fieldTypes.put(fieldName, fieldType);
    }

    /**
     * Set field type forcibly by table name.
     *
     * @param tableName table name
     * @param fieldName field name
     * @param fieldType field type
     */
    public void asFieldType(String tableName, String fieldName, String fieldType) {
        tables
                .computeIfAbsent(tableName, key -> new TableConfig()).fieldTypes
                .put(fieldName, fieldType);
    }

    /**
     * Gets fields types.
     *
     * @param tableName table name
     * @return map field - type
     */
    public Map<String, String> getFieldTypes(String tableName) {
        Map<String, String> result = new HashMap<>(common.fieldTypes);
        TableConfig table = tables.get(tableName);
        if (table != null) {
            result.putAll(table.fieldTypes);
        }
        return result;
    }

    /**
     * Gets fields names.
     *
     * @param tableName table name
     * @return map field - name
     */
    public Map<String, String> getFieldNames(String tableName) {
        Map<String, String> result = new HashMap<>(common.fieldNames);
        TableConfig table = tables.get(tableName);
        if (table != null) {
            result.putAll(table.fieldNames);
        }
        return result;
    }

    /**
     * Gets fields defaults.
     *
     * @param tableName table name
     * @return map field - name
     */
    public Map<String, String> getFieldDefaults(String tableName) {
        Map<String, String> result = new HashMap<>(common.fieldDefaults);
        TableConfig table = tables.get(tableName);
        if (table != null) {
            result.putAll(table.fieldDefaults);
        }
        return result;
    }

    /**
     * Gets type defaults.
     *
     * @param tableName table name
     * @return map field - name
     */
    public Map<String, String> getTypeDefaults(String tableName) {
        Map<String, String> result = new HashMap<>(common.typeDefaults);
        TableConfig table = tables.get(tableName);
        if (table != null) {
            result.putAll(table.typeDefaults);
        }
        return result;
    }

    /**
     * Gets columns sort order.
     *
     * @param tableName table name
     * @return map field - name
     */
    public TableConfig.ColumnOrder getColumnsOrder(String tableName) {
        TableConfig table = tables.get(tableName);
        if ((table != null) && (table.columnsOrder != null)) {
            return table.columnsOrder;
        }
        return common.columnsOrder != null ? common.columnsOrder : TableConfig.ColumnOrder.TABLE;
    }

    /**
     * Gets columns sort order.
     *
     * @param tableName table name
     * @return map field - name
     */
    public boolean isUseDefaults(String tableName) {
        TableConfig table = tables.get(tableName);
        if ((table != null) && (table.useDefaults != null)) {
            return table.useDefaults;
        }
        return common.useDefaults != null ? common.useDefaults : false;
    }

    /** Configuration checker. */
    public void check() {
        if (Strings.isNullOrEmpty(dbURL)) {
            throw new IllegalArgumentException("[dbURL] not defined");
        }
        if (Strings.isNullOrEmpty(dbUser)) {
            throw new IllegalArgumentException("[dbUser] not defined");
        }
    }
}
