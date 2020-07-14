package org.wwapp.db2dto;

import com.google.common.base.CaseFormat;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.ToString;

/** @author Walery Wysotsky <dev@wysotsky.info> */
@ToString
public class DBColumn {

  public String name;
  public String javaFieldName;
  public String javaPropertyName;
  public int sqlType;
  public String sqlTypeName;
  public String javaType;
  public int size;
  public int digits;
  public boolean isNullable;
  public String description;

  /**
   * Convert result of Metadata.getColumns to column info object.
   *
   * @param rs
   */
  public DBColumn(ResultSet rs) throws SQLException {
    name = rs.getString("COLUMN_NAME");
    javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
    javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toLowerCase());
    sqlType = rs.getInt("DATA_TYPE");
    sqlTypeName = rs.getString("TYPE_NAME");
    size = rs.getInt("COLUMN_SIZE");
    digits = rs.getInt("DECIMAL_DIGITS");
    isNullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
    description = rs.getString("REMARKS");
  }
}
