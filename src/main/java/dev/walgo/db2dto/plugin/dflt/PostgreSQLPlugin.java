package dev.walgo.db2dto.plugin.dflt;

import dev.walgo.db2dto.DBColumn;
import dev.walgo.db2dto.plugin.IPlugin;
import dev.walgo.walib.TriOptional;
import dev.walgo.walib.db.DBInfo;
import dev.walgo.walib.db.DBType;
import dev.walgo.walib.db.DBUtils;
import java.sql.Types;

public class PostgreSQLPlugin implements IPlugin {

    @Override
    public boolean usePlugin(DBInfo info) {
        return DBType.POSTGRESQL == info.getDBType();
    }

    @Override
    public boolean fillJavaType(DBColumn column) {
        if ((column.defaultValue != null) && DBUtils.isStringField(column.sqlType)) {
            int idx = column.defaultValue.indexOf("::");
            if (idx >= 0) {
                String result = column.defaultValue.substring(0, idx);
                if (column.defaultValue.endsWith("'")) {
                    result += "'";
                }
                column.defaultValue = result;
            }
        }
        if (column.sqlType == Types.BIT) {
// PostgreSQL boolean/bit workaround          
            column.sqlType = Types.BOOLEAN;
        } else if ("uuid".equals(column.sqlTypeName)) {
            column.javaType = "java.util.UUID";
            column.simpleJavaType = "java.util.UUID";
            column.defaultValue = null; // "new java.util.UUID(0l, 0l)";
            column.isSimpleType = false;
            return true;
        }
        return false;
    }

    @Override
    public TriOptional<String> getDefaultValue(DBColumn column) {
        if ("uuid".equals(column.sqlTypeName)) {
            return TriOptional.of(null);
        }
        return TriOptional.empty();
    }

}
