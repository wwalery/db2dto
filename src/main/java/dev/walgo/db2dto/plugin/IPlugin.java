package dev.walgo.db2dto.plugin;

import dev.walgo.db2dto.DBColumn;
import dev.walgo.walib.TriOptional;
import dev.walgo.walib.db.DBInfo;

public interface IPlugin {

    boolean usePlugin(DBInfo info);

    /**
     * Fill fields {@link DBColumn#javaType } and {@link DBColumn#simpleJavaType } from SQL type.
     *
     * @param column
     * @return TRUE if fields filled
     */
    boolean fillJavaType(DBColumn column);

    /**
     * Gets default value for column.
     *
     * <p>
     * Not default value from DB, but value for replace field null value.
     *
     * @param column
     * @return value for column. String values need to be returned with double quotes
     */
    TriOptional<String> getDefaultValue(DBColumn column);
}
