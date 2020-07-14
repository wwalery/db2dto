package org.wwapp.db2dto;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;
import lombok.Setter;

/**
 * Common configuration.
 *
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class Config {

  // DB config
  /**
   * URL to database, required.
   */
  public String dbURL;
  /**
   * Database user, required.
   */
  public String dbUser;
  /**
   * Database password, optional.
   */
  public String dbPassword = "";

  /**
   * Java package name for generated classes.
   *
   * <p>
   * 'dto' by default
   */
  @Setter
  private String packageName = "dto";

  /**
   * Java package name for generated classes per table name.
   */
  private final Map<String, String> packageNames = new HashMap<>();

  /**
   * Output directory for generated classes.
   *
   * <p>
   * './build/generated.dto' by default
   */
  public String outputDir = "./build/generated.dto";

  /**
   * Name of base interface for all generated classes.
   *
   * <p>
   * 'IData' by default
   */
  public String baseInterfaceName = "IData";

  /**
   * Prefix for generated class name.
   *
   * <p>
   * empty by default
   */
  public String classPrefix = "";

  /**
   * Suffix for generated class name.
   *
   * <p>
   * 'Data' by default
   */
  public String classSuffix = "Data";

  /**
   * List of read-only fields, common for all tables.
   */
  @Getter
  Set<String> readOnlyFields = new TreeSet<>();

  /**
   * List of interfaces, for add to generated class by table.
   */
  private final Map<String, Set<Class<?>>> interfaces = new HashMap<>();

  /**
   * List of additional fields, for add to generated class by table.
   */
  private final Map<String, Map<String, Class<?>>> additionalFields = new HashMap<>();

  /**
   * Consider field as enumerate, for generated class by table.
   */
  private final Map<String, Map<String, Enum<?>>> enumFields = new HashMap<>();

  /**
   * Ignore this fields in 'toString' method by table.
   */
  private final Map<String, Set<String>> toStringIgnoreFields = new HashMap<>();

  private final static String DEFAULT_KEY = "";

  /**
   * Link package name to table name
   *
   * @param tableName
   * @param packageName
   */
  public void registerPackageName(String tableName, String packageName) {
    packageNames.put(tableName, packageName);
  }

  /**
   * Gets package name by table name or default.
   *
   * @param tableName
   * @return
   */
  public String getPackageName(String tableName) {
    return packageNames.getOrDefault(tableName, packageName);
  }

  public void registerReadOnlyField(String fieldName) {
    readOnlyFields.add(fieldName);
  }

  /**
   * Register default (common) interface, for add to generated class.
   *
   * @param interfaceClass
   */
  public void registerInterface(Class<?> interfaceClass) {
    registerInterface(DEFAULT_KEY, interfaceClass);
  }

  /**
   * Register interface by table name.
   *
   * @param tableName
   * @param interfaceClass
   */
  public void registerInterface(String tableName, Class<?> interfaceClass) {
    List<Class<?>> interfaceList = interfaces.computeIfAbsent(tableName, () -> new ArrayList<>());
    interfaceList.add(interfaceClass);
  }

  /**
   * Gets interface list by table name or default.
   *
   * @param tableName
   * @return
   */
  Set<Class<?>> getInterfaces(String tableName) {
    if (interfaces.containsKey(tableName)) {
      return interfaces.get(tableName);
    } else {
      return interfaces.getOrDefault(DEFAULT_KEY, Collections.EMPTY_SET);
    }
  }

  /**
   * Add field, for add to all generated class.
   *
   * @param fieldName
   * @param fieldType
   */
  public void addField(String fieldName, Class<?> fieldType) {
    addField(DEFAULT_KEY, fieldName, fieldType);
  }

  /**
   * Additional field (not existing int table) by table name.
   *
   * @param tableName
   * @param fieldName
   * @param fieldType
   */
  public void addField(String tableName, String fieldName, Class<?> fieldType) {
    Map<String, Class<?>> fieldMap = additionalFields.computeIfAbsent(tableName,
            () -> new HashMap<>());
    fieldMap.put(fieldName, fieldType);
  }

  /**
   * Gets additional fields list by table name or default.
   *
   * @param tableName
   * @return
   */
  Map<String, Class<?>> getFields(String tableName) {
    if (additionalFields.containsKey(tableName)) {
      return additionalFields.get(tableName);
    } else {
      return additionalFields.getOrDefault(DEFAULT_KEY, Collections.EMPTY_MAP);
    }
  }

  /**
   * Consider field as enumerate, for generated classes for all tables.
   *
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String fieldName, Enum enumType) {
    asEnum(DEFAULT_KEY, fieldName, enumType);
  }

  /**
   * Consider field as enumerate by table name.
   *
   * @param tableName
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String tableName, String fieldName, Enum<?> enumType) {
    Map<String, Enum<?>> fieldMap = enumFields.computeIfAbsent(tableName,
            () -> new HashMap<>());
    fieldMap.put(fieldName, enumType);
  }

  /**
   * Gets fields considered as enumerate.
   *
   * @param tableName
   * @return
   */
  Map<String, Enum<?>> getEnums(String tableName) {
    if (enumFields.containsKey(tableName)) {
      return enumFields.get(tableName);
    } else {
      return enumFields.getOrDefault(DEFAULT_KEY, Collections.EMPTY_MAP);
    }
  }

  /**
   * Ignore this fields in 'toString' method for all tables.
   */
  /**
   * Add field, for add to all generated class.
   *
   * @param fieldName
   */
  public void addToStringIgnore(String fieldName) {
    addToStringIgnore(DEFAULT_KEY, fieldName);
  }

  /**
   * Gets list of fields, ignored in 'toString' method by table.
   *
   * @param tableName
   * @param fieldName
   */
  public void addToStringIgnore(String tableName, String fieldName) {
    List<String> fieldList = toStringIgnoreFields.computeIfAbsent(tableName,
            () -> new ArrayList<>());
    fieldList.add(fieldName);
  }

  /**
   * Gets additional fields list by table name or default.
   *
   * @param tableName
   * @return
   */
  Set<String> getToStringIgnoredFields(String tableName) {
    if (toStringIgnoreFields.containsKey(tableName)) {
      return toStringIgnoreFields.get(tableName);
    } else {
      return toStringIgnoreFields.getOrDefault(DEFAULT_KEY, Collections.EMPTY_SET);
    }
  }

  void check() {
    if (Strings.isNullOrEmpty(dbURL)) {
      throw new IllegalArgumentException("[dbURL] not defined");
    }
    if (Strings.isNullOrEmpty(dbUser)) {
      throw new IllegalArgumentException("[dbUser] not defined");
    }
  }
}
