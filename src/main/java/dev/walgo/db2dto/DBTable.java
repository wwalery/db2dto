package dev.walgo.db2dto;

import com.google.common.base.CaseFormat;
import dev.walgo.db2dto.config.Config;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.ToString;

@ToString
public class DBTable {

    public String name;
    public String realName;
    public String javaName;
    public String type;

    public List<DBColumn> columns;

    public DBTable(ResultSet rs) throws SQLException {
        realName = rs.getString("TABLE_NAME");
        type = rs.getString("TABLE_TYPE");
        name = realName.toLowerCase();
        javaName = Config.getCONFIG().getClassPrefix(name)
                + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)
                + Config.getCONFIG().getClassSuffix(name);
    }
}
