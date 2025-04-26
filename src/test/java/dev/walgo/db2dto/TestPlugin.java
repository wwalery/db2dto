package dev.walgo.db2dto;

import dev.walgo.db2dto.plugin.IPlugin;
import dev.walgo.walib.TriOptional;
import dev.walgo.walib.db.DBInfo;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class TestPlugin implements IPlugin {

    private static final String TABLE_2 = "test_table_2";
    private static final String FIELD_OBJECT = "test_object";

    @Override
    public boolean fillJavaType(DBColumn column) {
        if (TABLE_2.equals(column.tableName) && FIELD_OBJECT.equals(column.name)) {
            column.javaType = TestClass.class.getName();
            column.simpleJavaType = TestClass.class.getName();
            return true;
        }
        return false;
    }

    @Override
    public TriOptional<String> getDefaultValue(DBColumn column) {
        if (TABLE_2.equals(column.tableName) && FIELD_OBJECT.equals(column.name)) {
            return TriOptional.of("new " + TestClass.class.getName() + "()");
        }
        return TriOptional.empty();
    }

    @Override
    public boolean usePlugin(DBInfo info) {
        return true;
    }
}
