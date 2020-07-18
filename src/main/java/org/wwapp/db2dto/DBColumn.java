package org.wwapp.db2dto;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.wwapp.db2dto.config.Config;
import org.wwapp.db2dto.plugin.IPlugin;
import org.wwapp.db2dto.plugin.PluginHandler;

/** @author Walery Wysotsky <dev@wysotsky.info> */
@ToString
@Slf4j
public class DBColumn {

  private static final String BIG_DECIMAL = "java.math.BigDecimal";
  private static final String STRING = "String";
  private static final String BOOLEAN = "Boolean";

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
      javaType = guessJavaType();
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
        return "Long";
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
        return "java.sql.Date";
      case Types.DECIMAL:
      case Types.NUMERIC:
        if (digits == 0) {
          return "java.math.BigInteger";
        } else {
          return BIG_DECIMAL;
        }
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:
        return BIG_DECIMAL;
      case Types.INTEGER:
      case Types.SMALLINT:
        return "Integer";
      case Types.TIME:
        return "java.sql.Time";
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return "java.sql.Timestamp";
      case Types.TINYINT:
        return "Byte";
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        return "byte[]";
      default:
        LOG.warn("Undefined java type for field [{}]", this);
        return STRING;
    }
  }
}
