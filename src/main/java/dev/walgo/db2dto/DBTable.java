package dev.walgo.db2dto;

import com.google.common.base.CaseFormat;
import dev.walgo.db2dto.config.Config;
import dev.walgo.walib.db.TableInfo;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DBTable {

    public String name;
    public String realName;
    public String javaName;
    public String type;

    public List<DBColumn> columns;

    public DBTable(TableInfo tableInfo) {
        realName = tableInfo.getName();
        type = tableInfo.getType();
        name = realName.toLowerCase();
        javaName = Config.getCONFIG().getClassPrefix(name)
                + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)
                + Config.getCONFIG().getClassSuffix(name);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).toString();
    }

}
