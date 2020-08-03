package dev.walgo.db2dto.plugin;

import dev.walgo.db2dto.DBColumn;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public interface IPlugin {

  /**
   * Custom java type from SQL retriever.
   *
   * @param column
   * @return Java type or null/empty
   */
  String getJavaType(DBColumn column);

  /**
   * Gets default value for column.
   *
   * <p>Not default value from DB, but value for replace field null value.
   *
   * @param column
   * @return value for column. String values need to be returned with double quotes
   */
  String getDefaultValue(DBColumn column);
}
