package org.wwapp.db2dto;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.Setter;

/**
 * Common configuration.
 *
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class Config {

  private static final String DEFAULT_KEY = "";

  // DB config
  /** URL to database, required. */
  public String dbURL;
  /** Database user, required. */
  public String dbUser;
  /** Database password, optional. */
  public String dbPassword = "";

  /**
   * Output directory for generated classes.
   *
   * <p>'./build/generated.dto' by default
   */
  public String outputDir = "./build/generated.dto";

  /**
   * Name of base interface for all generated classes.
   *
   * <p>'IData' by default
   */
  public String baseInterfaceName = "IData";

  /**
   * Prefix for generated class name.
   *
   * <p>empty by default
   */
  public String classPrefix = "";

  /**
   * Suffix for generated class name.
   *
   * <p>'Data' by default
   */
  public String classSuffix = "Data";

  /** List of read-only fields, common for all tables. */
  Set<String> readOnlyFields = new TreeSet<>();

  /**
   * Java package name for generated classes.
   *
   * <p>'dto' by default
   */
  @Setter private String packageName = "dto";

  /** Java package name for generated classes per table name. */
  private final Map<String, String> packageNames = new HashMap<>();

  /** List of interfaces, for add to generated class by table. */
  private final Map<String, Set<String>> interfaces = new HashMap<>();

  /** List of additional fields, for add to generated class by table. */
  private final Map<String, Map<String, String>> additionalFields = new HashMap<>();

  /** Consider field as enumerate, for generated class by table. */
  private final Map<String, Map<String, String>> enumFields = new HashMap<>();

  /** Ignore this fields in 'toString' method by table. */
  private final Map<String, Set<String>> toStringIgnoreFields = new HashMap<>();

  /**
   * Link package name to table name
   *
   * @param tableName
   * @param packageNameValue
   */
  public void registerPackageName(String tableName, String packageNameValue) {
    packageNames.put(tableName, packageNameValue);
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
  public void registerInterface(String interfaceClass) {
    registerInterface(DEFAULT_KEY, interfaceClass);
  }

  /**
   * Register interface by table name.
   *
   * @param tableName
   * @param interfaceClass
   */
  public void registerInterface(String tableName, String interfaceClass) {
    Set<String> interfaceList = interfaces.computeIfAbsent(tableName, key -> new HashSet<>());
    interfaceList.add(interfaceClass);
  }

  /**
   * Gets interface list by table name or default.
   *
   * @param tableName
   * @return
   */
  Set<String> getInterfaces(String tableName) {
    if (interfaces.containsKey(tableName)) {
      return interfaces.get(tableName);
    } else {
      return interfaces.getOrDefault(DEFAULT_KEY, Collections.<String>emptySet());
    }
  }

  /**
   * Add field, for add to all generated class.
   *
   * @param fieldName
   * @param fieldType
   */
  public void addField(String fieldName, String fieldType) {
    addField(DEFAULT_KEY, fieldName, fieldType);
  }

  /**
   * Additional field (not existing int table) by table name.
   *
   * @param tableName
   * @param fieldName
   * @param fieldType
   */
  public void addField(String tableName, String fieldName, String fieldType) {
    Map<String, String> fieldMap =
        additionalFields.computeIfAbsent(tableName, key -> new HashMap<>());
    fieldMap.put(fieldName, fieldType);
  }

  /**
   * Gets additional fields list by table name or default.
   *
   * @param tableName
   * @return
   */
  Map<String, String> getFields(String tableName) {
    if (additionalFields.containsKey(tableName)) {
      return additionalFields.get(tableName);
    } else {
      return additionalFields.getOrDefault(DEFAULT_KEY, Collections.<String, String>emptyMap());
    }
  }

  /**
   * Consider field as enumerate, for generated classes for all tables.
   *
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String fieldName, String enumType) {
    asEnum(DEFAULT_KEY, fieldName, enumType);
  }

  /**
   * Consider field as enumerate by table name.
   *
   * @param tableName
   * @param fieldName
   * @param enumType
   */
  public void asEnum(String tableName, String fieldName, String enumType) {
    Map<String, String> fieldMap = enumFields.computeIfAbsent(tableName, key -> new HashMap<>());
    fieldMap.put(fieldName, enumType);
  }

  /**
   * Gets fields considered as enumerate.
   *
   * @param tableName
   * @return
   */
  Map<String, String> getEnums(String tableName) {
    if (enumFields.containsKey(tableName)) {
      return enumFields.get(tableName);
    } else {
      return enumFields.getOrDefault(DEFAULT_KEY, Collections.<String, String>emptyMap());
    }
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
    addToStringIgnore(DEFAULT_KEY, fieldName);
  }

  /**
   * Gets list of fields, ignored in 'toString' method by table.
   *
   * @param tableName
   * @param fieldName
   */
  public void addToStringIgnore(String tableName, String fieldName) {
    Set<String> fieldList = toStringIgnoreFields.computeIfAbsent(tableName, key -> new HashSet<>());
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
      return toStringIgnoreFields.getOrDefault(DEFAULT_KEY, Collections.<String>emptySet());
    }
  }

  public boolean isReadOnlyField(final String fieldName) {
    return readOnlyFields.contains(fieldName);
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
