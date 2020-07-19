package dev.walgo.db2dto;

import dev.walgo.db2dto.plugin.IPlugin;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class TestPlugin implements IPlugin {

  private static final String TABLE_2 = "test_table_2";
  private static final String FIELD_OBJECT = "test_object";

  @Override
  public String getJavaType(DBColumn column) {
    if (TABLE_2.equals(column.tableName) && FIELD_OBJECT.equals(column.name)) {
      return TestClass.class.getName();
    }
    return null;
  }
}
