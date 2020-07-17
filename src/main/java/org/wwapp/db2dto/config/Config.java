package org.wwapp.db2dto.config;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Getter;
import org.wwapp.db2dto.DBColumn;
import org.wwapp.db2dto.DBTable;
import org.wwapp.db2dto.IPlugin;

/**
 * Common configuration.
 *
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class Config {

  @Getter private static Config CONFIG;

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
   * <p>'./examples' by default
   */
  public String templateDir = "./templates";

  /**
   * Output directory for generated source.
   *
   * <p>'./build/dto/generated' by default
   */
  public String sourceOutputDir = "./build/dto/generated";

  /** compile generated source to jar. */
  public boolean compile;

  /**
   * Output directory for compiled classes.
   *
   * <p>'./build/dto/classes' by default
   */
  public String classOutputDir = "./build/dto/classes";

  /** Compiled jar name. */
  public String jarPath = "build/db2dto_generated.jar";

  /**
   * Name of base interface for all generated classes.
   *
   * <p>'IData' by default
   */
  public String baseInterfaceName = "IData";

  /** Common data for all tables. */
  public final TableConfig common;

  public final Map<String, TableConfig> tables;

  @Getter private List<IPlugin> plugins = new ArrayList<>();

  public Config() {
    this.common = new TableConfig();
    this.tables = new HashMap<>();
    this.common.classPrefix = "";
    this.common.classSuffix = "Data";
    this.common.packageName = "dto";
    CONFIG = this;
    //    this.common.readOnlyFields = new TreeSet<>();
    //    this.common.interfaces = new HashSet<>();
    //    this.common.additionalFields = new HashMap<>();
    //    this.common.enumFields = new HashMap<>();
    //    this.common.toStringIgnoreFields = new HashSet<>();
  }

  public void setClassPrefix(String classPrefixValue) {
    common.classPrefix = classPrefixValue;
  }

  public void setClassPrefix(String tableName, String classPrefixValue) {
    tables.computeIfAbsent(tableName, key -> new TableConfig()).classPrefix = classPrefixValue;
  }

  public String getClassPrefix(String tableName) {
    TableConfig table = tables.get(tableName);
    if ((table != null) && (table.classPrefix != null)) {
      return table.classPrefix;
    } else {
      return common.classPrefix;
    }
  }

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
   * @param tableName
   * @param packageNameValue
   */
  public void setPackageName(String tableName, String packageNameValue) {
    tables.computeIfAbsent(tableName, key -> new TableConfig()).packageName = packageNameValue;
  }

  /**
   * Gets package name by table name or default.
   *
   * @param tableName
   * @return
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
   * @param interfaceClass
   */
  public void useInterface(String interfaceClass) {
    common.interfaces.add(interfaceClass);
  }

  /**
   * Register interface by table name.
   *
   * @param tableName
   * @param interfaceClass
   */
  public void useInterface(String tableName, String interfaceClass) {
    tables.computeIfAbsent(tableName, key -> new TableConfig()).interfaces.add(interfaceClass);
  }

  /**
   * Gets interface list by table name or default.
   *
   * @param tableName
   * @return
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
   * @param fieldName
   * @param fieldType
   */
  public void addField(String fieldName, String fieldType) {
    common.additionalFields.put(fieldName, fieldType);
  }

  /**
   * Additional field (not existing int table) by table name.
   *
   * @param tableName
   * @param fieldName
   * @param fieldType
   */
  public void addField(String tableName, String fieldName, String fieldType) {
    tables
        .computeIfAbsent(tableName, key -> new TableConfig())
        .additionalFields
        .put(fieldName, fieldType);
  }

  /**
   * Gets additional fields list by table name or default.
   *
   * @param tableName
   * @return
   */
  public List<DBColumn> getFields(String tableName) {
    TableConfig table = tables.get(tableName);
    List<DBColumn> fields =
        new ArrayList<>(
            common.additionalFields.entrySet().stream()
                .map(it -> new DBColumn(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));
    if (table != null) {
      fields.addAll(
          table.additionalFields.entrySet().stream()
              .map(it -> new DBColumn(it.getKey(), it.getValue()))
              .collect(Collectors.toList()));
    }
    return fields;
  }

  /**
   * Consider field as enumerate, for generated classes for all tables.
   *
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String fieldName, String enumType) {
    common.enumFields.put(fieldName, enumType);
  }

  /**
   * Consider field as enumerate by table name.
   *
   * @param tableName
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String tableName, String fieldName, String enumType) {
    tables.computeIfAbsent(tableName, key -> new TableConfig()).enumFields.put(fieldName, enumType);
  }

  /**
   * Gets fields considered as enumerate.
   *
   * @param tableName
   * @return
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
   * @param fieldName
   */
  public void addToStringIgnore(String fieldName) {
    common.toStringIgnoreFields.add(fieldName);
  }

  /**
   * Gets list of fields, ignored in 'toString' method by table.
   *
   * @param tableName
   * @param fieldName
   */
  public void addToStringIgnore(String tableName, String fieldName) {
    tables.computeIfAbsent(tableName, key -> new TableConfig()).toStringIgnoreFields.add(fieldName);
  }

  /**
   * Gets additional fields list by table name or default.
   *
   * @param tableName
   * @return
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
    List<DBColumn> result =
        new ArrayList<>(
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

  /**
   * Set field type forcibly for all tables.
   *
   * @param fieldName
   * @param enumType
   */
  public void asFieldType(String fieldName, String fieldType) {
    common.fieldTypes.put(fieldName, fieldType);
  }

  /**
   * Set field type forcibly by table name.
   *
   * @param tableName
   * @param fieldName
   * @paramfieldTypeenumType
   */
  public void asFieldType(String tableName, String fieldName, String fieldType) {
    tables
        .computeIfAbsent(tableName, key -> new TableConfig())
        .fieldTypes
        .put(fieldName, fieldType);
  }

  /**
   * Gets fields types.
   *
   * @param tableName
   * @return
   */
  public Map<String, String> getFieldTypes(String tableName) {
    Map<String, String> result = new HashMap<>(common.fieldTypes);
    TableConfig table = tables.get(tableName);
    if (table != null) {
      result.putAll(table.fieldTypes);
    }
    return result;
  }

  public void registerPlugin(IPlugin plugin) {
    plugins.add(plugin);
  }

  public void check() {
    if (Strings.isNullOrEmpty(dbURL)) {
      throw new IllegalArgumentException("[dbURL] not defined");
    }
    if (Strings.isNullOrEmpty(dbUser)) {
      throw new IllegalArgumentException("[dbUser] not defined");
    }
  }
}
