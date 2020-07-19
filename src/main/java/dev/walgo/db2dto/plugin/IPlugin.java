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
}
