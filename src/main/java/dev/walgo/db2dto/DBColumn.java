package dev.walgo.db2dto;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import dev.walgo.db2dto.config.Config;
import dev.walgo.db2dto.plugin.IPlugin;
import dev.walgo.db2dto.plugin.PluginHandler;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class DBColumn {

  private static final String BIG_DECIMAL = "java.math.BigDecimal";
  private static final String BIG_INTEGER = "java.math.BigInteger";
  private static final String STRING = "String";
  private static final String BOOLEAN = "Boolean";
  private static final String BOOLEAN_SIMPLE = "boolean";
  private static final String BYTE_ARRAY = "byte[]";
  private static final String BYTE = "Byte";
  private static final String BYTE_SIMPLE = "byte";
  private static final String SQL_TIMESTAMP = "java.sql.Timestamp";
  private static final String SQL_TIME = "java.sql.Time";
  private static final String LOCAL_DATE_TIME = "LocalDateTime";
  private static final String INSTANT = "Instant";
  private static final String LOCAL_DATE = "LocalDate";
  private static final String INTEGER = "Integer";
  private static final String INTEGER_SIMPLE = "int";
  private static final String SQL_DATE = "java.sql.Date";
  private static final String LONG = "Long";
  private static final String LONG_SIMPLE = "long";
  private static final String NEW = "new ";
  private static final String NEW_OBJECT = "new %s()";
  private static final String ARRAY_BRACKETS = "[]";

  private static final String VALUE_BOOLEAN = "false";
  private static final String VALUE_BIG_DECIMAL = "java.math.BigDecimal.ZERO";
  private static final String VALUE_STRING = "\"\"";

  public String name;
  public String tableName;
  public String javaFieldName;
  public String javaPropertyName;
  public int sqlType;
  public String sqlTypeName;
  public String javaType;
  public String simpleJavaType;
  public boolean isSimpleType;
  public int size;
  public int digits;
  public boolean isNullable;
  public String description;

  public DBColumn(String fieldName, String fieldJavaType) {
    name = fieldName.toLowerCase();
    javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
    javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toLowerCase());
    javaType = fieldJavaType;
    simpleJavaType = fieldJavaType;
    isSimpleType =
        fieldJavaType.equals(BOOLEAN_SIMPLE)
            || fieldJavaType.equals(BYTE_SIMPLE)
            || fieldJavaType.equals(INTEGER_SIMPLE)
            || fieldJavaType.equals(LONG_SIMPLE);
    isNullable = !isSimpleType;
  }

  /**
   * Convert result of Metadata.getColumns to column info object.
   *
   * @param rs
   */
  public DBColumn(ResultSet rs) throws SQLException {
    name = rs.getString("COLUMN_NAME").toLowerCase();
    tableName = rs.getString("TABLE_NAME").toLowerCase();
    String alias = Config.getCONFIG().getFieldNames(tableName).getOrDefault(name, name);
    setJavaNames(alias);
    sqlType = rs.getInt("DATA_TYPE");
    sqlTypeName = rs.getString("TYPE_NAME");
    size = rs.getInt("COLUMN_SIZE");
    digits = rs.getInt("DECIMAL_DIGITS");
    isNullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
    description = rs.getString("REMARKS");
    javaType = Config.getCONFIG().getFieldTypes(tableName).get(name);
    if (javaType == null) {
      String sqlMappedType = Config.getCONFIG().sqlTypes.get(sqlTypeName);
      if (sqlMappedType == null) {
        guessJavaType();
      } else {
        javaType = sqlMappedType;
        if (sqlType == Types.ARRAY) {
          javaType =
              Config.getCONFIG().arrayAsList ? "List<" + javaType + ">" : javaType + ARRAY_BRACKETS;
        }
        simpleJavaType = javaType;
      }
    } else {
      simpleJavaType = javaType;
    }
  }

  private void setJavaNames(final String fieldName) {
    String nameLC = fieldName.toLowerCase();
    javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, nameLC);
    javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, nameLC);
  }

  private void guessJavaType() {
    for (IPlugin plugin : PluginHandler.getPlugins()) {
      if (plugin.fillJavaType(this)) {
        return;
      }
    }
    switch (sqlType) {
      case Types.BIGINT:
        javaType = LONG;
        simpleJavaType = LONG_SIMPLE;
        isSimpleType = !isNullable;
        break;
      case Types.BIT:
        javaType = BOOLEAN;
        simpleJavaType = BOOLEAN_SIMPLE;
        isSimpleType = !isNullable;
        break;
      case Types.BOOLEAN:
        javaType = BOOLEAN;
        simpleJavaType = BOOLEAN_SIMPLE;
        isSimpleType = !isNullable;
        break;
      case Types.CHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.VARCHAR:
        javaType = STRING;
        simpleJavaType = STRING;
        break;
      case Types.DATE:
        javaType = SQL_DATE;
        simpleJavaType = SQL_DATE;
        break;
      case Types.DECIMAL:
      case Types.NUMERIC:
        if (digits == 0) {
          javaType = BIG_INTEGER;
          simpleJavaType = BIG_INTEGER;
        } else {
          javaType = BIG_DECIMAL;
          simpleJavaType = BIG_DECIMAL;
        }
        break;
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:
        javaType = BIG_DECIMAL;
        simpleJavaType = BIG_DECIMAL;
        break;
      case Types.INTEGER:
      case Types.SMALLINT:
        javaType = INTEGER;
        simpleJavaType = INTEGER_SIMPLE;
        isSimpleType = !isNullable;
        break;
      case Types.TIME:
        javaType = SQL_TIME;
        simpleJavaType = SQL_TIME;
        break;
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        javaType = SQL_TIMESTAMP;
        simpleJavaType = SQL_TIMESTAMP;
        break;
      case Types.TINYINT:
        javaType = BYTE;
        simpleJavaType = BYTE_SIMPLE;
        isSimpleType = !isNullable;
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        javaType = BYTE_ARRAY;
        simpleJavaType = BYTE_ARRAY;
        break;
      default:
        LOG.warn("Undefined sql type for field [{}]", this);
        javaType = STRING;
        simpleJavaType = STRING;
    }
  }

  public String getDefaultValue() {
    for (IPlugin plugin : PluginHandler.getPlugins()) {
      String result = plugin.getDefaultValue(this);
      if (!Strings.isNullOrEmpty(result)) {
        return result;
      }
    }
    String defaultValue = Config.getCONFIG().getFieldDefaults(tableName).get(name);
    if (defaultValue != null) {
      return defaultValue;
    }
    defaultValue = Config.getCONFIG().getTypeDefaults(tableName).get(sqlTypeName);
    if (defaultValue != null) {
      return defaultValue;
    }

    String predefinedType = Config.getCONFIG().getFieldTypes(tableName).get(name);
    if (predefinedType != null) {
      return String.format(NEW_OBJECT, predefinedType);
    }

    switch (javaType) {
      case LONG:
        return "0L";
      case BOOLEAN:
        return VALUE_BOOLEAN;
      case STRING:
        return VALUE_STRING;
      case SQL_DATE:
        return "java.sql.Date.valueOf(java.time.LocalDate.now())";
      case BIG_INTEGER:
        return "java.math.BigInteger.ZERO";
      case BIG_DECIMAL:
        return VALUE_BIG_DECIMAL;
      case INTEGER:
        return "0";
      case SQL_TIME:
        return "java.sql.Time.valueOf(java.time.LocalTime.now())";
      case SQL_TIMESTAMP:
        return "java.sql.Timestamp.from(java.time.Instant.now())";
      case LOCAL_DATE:
        return "LocalDate.now()";
      case LOCAL_DATE_TIME:
        return "LocalDateTime.now()";
      case INSTANT:
        return "Instant.now()";
      case BYTE:
        return "(byte) 0";
      case BYTE_ARRAY:
        return "new byte[0]";
      default:
        String test = "Map";
        if (javaType.startsWith(test)) {
          return "new HashMap<>()";
        }
        test = "List";
        if (javaType.startsWith(test)) {
          return "new ArrayList<>()";
        }
        test = "Set";
        if (javaType.startsWith(test)) {
          return "new HashSet<>()";
        }
        LOG.warn("Undefined java type for get default value for field [{}]", this);
        if (sqlType == Types.ARRAY) {
          return Config.getCONFIG().arrayAsList
              ? "(new ArrayList<>())"
              : NEW + javaType.replace(ARRAY_BRACKETS, "[0]");
        } else {
          return String.format(NEW_OBJECT, javaType);
        }
    }
  }
}
