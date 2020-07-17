package org.wwapp.db2dto;

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
