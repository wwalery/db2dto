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

/** @author Walery Wysotsky <dev@wysotsky.info> */
@ToString
@Slf4j
public class DBColumn {

  private static final String BIG_DECIMAL = "java.math.BigDecimal";
  private static final String BIG_INTEGER = "java.math.BigInteger";
  private static final String STRING = "String";
  private static final String BOOLEAN = "Boolean";
  private static final String BYTE_ARRAY = "byte[]";
  private static final String BYTE = "Byte";
  private static final String SQL_TIMESTAMP = "java.sql.Timestamp";
  private static final String SQL_TIME = "java.sql.Time";
  private static final String INTEGER = "Integer";
  private static final String SQL_DATE = "java.sql.Date";
  private static final String LONG = "Long";

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
  public int size;
  public int digits;
  public boolean isNullable;
  public String description;

  public DBColumn(String fieldName, String fieldJavaType) {
    name = fieldName.toLowerCase();
    javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
    javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toLowerCase());
    javaType = fieldJavaType;
  }

  /**
   * Convert result of Metadata.getColumns to column info object.
   *
   * @param rs
   */
  public DBColumn(ResultSet rs) throws SQLException {
    name = rs.getString("COLUMN_NAME").toLowerCase();
    tableName = rs.getString("TABLE_NAME").toLowerCase();
    javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
    javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toLowerCase());
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
        javaType = guessJavaType();
      } else {
        javaType = sqlMappedType;
        if (sqlType == Types.ARRAY) {
          javaType = Config.getCONFIG().arrayAsList ? "List<" + javaType + ">" : javaType + "[]";
        }
      }
    }
  }

  private String guessJavaType() {
    for (IPlugin plugin : PluginHandler.getPlugins()) {
      String result = plugin.getJavaType(this);
      if (!Strings.isNullOrEmpty(result)) {
        return result;
      }
    }
    switch (sqlType) {
      case Types.BIGINT:
        return LONG;
      case Types.BIT:
        return BOOLEAN;
      case Types.BOOLEAN:
        return BOOLEAN;
      case Types.CHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.VARCHAR:
        return STRING;
      case Types.DATE:
        return SQL_DATE;
      case Types.DECIMAL:
      case Types.NUMERIC:
        if (digits == 0) {
          return BIG_INTEGER;
        } else {
          return BIG_DECIMAL;
        }
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:
        return BIG_DECIMAL;
      case Types.INTEGER:
      case Types.SMALLINT:
        return INTEGER;
      case Types.TIME:
        return SQL_TIME;
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return SQL_TIMESTAMP;
      case Types.TINYINT:
        return BYTE;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        return BYTE_ARRAY;
      default:
        LOG.warn("Undefined sql type for field [{}]", this);
        return STRING;
    }
  }

  public String getDefaultValue() {
    for (IPlugin plugin : PluginHandler.getPlugins()) {
      String result = plugin.getDefaultValue(this);
      if (!Strings.isNullOrEmpty(result)) {
        return result;
      }
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
        return "java.sql.Timestamp.valueOf(java.time.LocalDateTime.now())";
      case BYTE:
        return "(byte) 0";
      case BYTE_ARRAY:
        return "new byte[0]";
      default:
        LOG.warn("Undefined java type for get default value for field [{}]", this);
        return "new " + javaType + "()";
    }
  }
}
