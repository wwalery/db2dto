package org.wwapp.db2dto.plugin;

import org.wwapp.db2dto.DBColumn;

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
