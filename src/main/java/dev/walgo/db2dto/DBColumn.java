package dev.walgo.db2dto;

import com.google.common.base.CaseFormat;
import dev.walgo.db2dto.config.Config;
import dev.walgo.db2dto.plugin.IPlugin;
import dev.walgo.db2dto.plugin.PluginHandler;
import dev.walgo.walib.TriOptional;
import dev.walgo.walib.db.ColumnInfo;
import dev.walgo.walib.db.DBInfo;
import java.sql.Types;
import java.util.Locale;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBColumn {

    private static final Logger LOG = LoggerFactory.getLogger(DBColumn.class);

    private static final String BIG_DECIMAL = "java.math.BigDecimal";
    private static final String BIG_INTEGER = "java.math.BigInteger";
    private static final String STRING = "String";
    private static final String BOOLEAN = "Boolean";
    private static final String BOOLEAN_SIMPLE = "boolean";
    private static final String BYTE_ARRAY = "byte[]";
    private static final String BYTE = "Byte";
    private static final String BYTE_SIMPLE = "byte";
    private static final String SQL_TIMESTAMP = "java.sql.Timestamp";
    private static final String SQL_TIME = "java.sql.Time";
    private static final String LOCAL_DATE_TIME = "LocalDateTime";
    private static final String INSTANT = "Instant";
    private static final String LOCAL_DATE = "LocalDate";
    private static final String INTEGER = "Integer";
    private static final String INTEGER_SIMPLE = "int";
    private static final String SQL_DATE = "java.sql.Date";
    private static final String LONG = "Long";
    private static final String LONG_SIMPLE = "long";
    private static final String NEW = "new ";
    private static final String NEW_OBJECT = "new %s()";
    private static final String ARRAY_BRACKETS = "[]";

    private static final String VALUE_BOOLEAN = "false";
    private static final String VALUE_BIG_DECIMAL = "java.math.BigDecimal.ZERO";
    private static final String VALUE_STRING = "\"\"";

    public String name;
    public String tableName;
    public String javaFieldName;
    public String javaPropertyName;
    public int sqlType;
    public String sqlTypeName;
    public String javaType;
    public String simpleJavaType;
    public boolean isSimpleType;
    public int size;
    public int digits;
    public boolean isNullable;
    public String description;
    public String defaultValue;
    public int order;

    @ToStringExclude
    private DBInfo dbInfo;

    public DBColumn(String fieldName, String fieldJavaType) {
        name = fieldName.toLowerCase();
        javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
        javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toLowerCase());
        javaType = fieldJavaType;
        simpleJavaType = fieldJavaType;
        isSimpleType = fieldJavaType.equals(BOOLEAN_SIMPLE)
                || fieldJavaType.equals(BYTE_SIMPLE)
                || fieldJavaType.equals(INTEGER_SIMPLE)
                || fieldJavaType.equals(LONG_SIMPLE);
        isNullable = !isSimpleType;
    }

    /**
     * Convert result of Metadata.getColumns to column info object.
     *
     * @param tableName
     * @param columnInfo
     */
    public DBColumn(String tableName, ColumnInfo columnInfo, DBInfo dbInfo) {
        this.dbInfo = dbInfo;
        this.name = columnInfo.name().toLowerCase(Locale.ROOT); // rs.getString("COLUMN_NAME").toLowerCase();
        this.tableName = tableName.toLowerCase(Locale.ROOT); // rs.getString("TABLE_NAME").toLowerCase();
        String alias = Config.getCONFIG().getFieldNames(this.tableName).getOrDefault(this.name, this.name);
        setJavaNames(alias);
        this.sqlType = columnInfo.type(); // rs.getInt("DATA_TYPE");
        this.sqlTypeName = columnInfo.typeName(); // rs.getString("TYPE_NAME");
        this.size = columnInfo.size(); // rs.getInt("COLUMN_SIZE");
        this.digits = columnInfo.digits(); // rs.getInt("DECIMAL_DIGITS");
        this.isNullable = columnInfo.isNullable(); // rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
        this.description = columnInfo.comment(); // rs.getString("REMARKS");
        this.defaultValue = columnInfo.defaultValue();
        this.order = columnInfo.position();
    }

    public void fillJavaType() {
        javaType = Config.getCONFIG().getFieldTypes(tableName).get(name);
        if (this.javaType == null) {
            String sqlMappedType = Config.getCONFIG().sqlTypes.get(sqlTypeName);
            if (sqlMappedType == null) {
                guessJavaType();
            } else {
                this.javaType = sqlMappedType;
                if (this.sqlType == Types.ARRAY) {
                    this.javaType = Config.getCONFIG().arrayAsList
                            ? "List<" + this.javaType + ">"
                            : this.javaType + ARRAY_BRACKETS;
                }
                this.simpleJavaType = this.javaType;
                this.defaultValue = null;
            }
        } else {
            this.simpleJavaType = this.javaType;
        }

    }

    private void setJavaNames(final String fieldName) {
        String nameLC = fieldName.toLowerCase();
        javaFieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, nameLC);
        javaPropertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, nameLC);
    }

    private void guessJavaType() {
//        LOG.warn("Field pre: [{}]", this);
        for (IPlugin plugin : PluginHandler.getPlugins(dbInfo)) {
            if (plugin.fillJavaType(this)) {
                return;
            }
        }
        switch (sqlType) {
            case Types.BIGINT:
                javaType = LONG;
                simpleJavaType = LONG_SIMPLE;
                isSimpleType = !isNullable;
                defaultValue = (defaultValue != null) && NumberUtils.isDigits(defaultValue)
                        ? defaultValue
                        : null;
                break;
            case Types.BIT:
                javaType = BOOLEAN;
                simpleJavaType = BOOLEAN_SIMPLE;
                isSimpleType = !isNullable;
                defaultValue = (defaultValue != null) && NumberUtils.isDigits(defaultValue)
                        ? (defaultValue.equals("0") ? "false" : "true")
                        : null;
                break;
            case Types.BOOLEAN:
                javaType = BOOLEAN;
                simpleJavaType = BOOLEAN_SIMPLE;
                // Set boolean to nullable and remove default value to avoid set the same value as default
                isNullable = true;
                defaultValue = null;
                isSimpleType = !isNullable;
                if (defaultValue != null) {
                    defaultValue = Boolean.toString(
                            defaultValue.equalsIgnoreCase("true") ||
                                    defaultValue.equalsIgnoreCase("y"));
                }
                break;
            case Types.CHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR:
                javaType = STRING;
                simpleJavaType = STRING;
                if (defaultValue != null) {
                    if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                        defaultValue = '"' + defaultValue.substring(1, defaultValue.length() - 1) + '"';
                    } else {
                        defaultValue = '"' + defaultValue + '"';
                    }
                }
                break;
            case Types.DATE:
                javaType = SQL_DATE;
                simpleJavaType = SQL_DATE;
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (digits == 0) {
                    javaType = BIG_INTEGER;
                    simpleJavaType = BIG_INTEGER;
                } else {
                    javaType = BIG_DECIMAL;
                    simpleJavaType = BIG_DECIMAL;
                }
                defaultValue = (defaultValue != null) && NumberUtils.isCreatable(defaultValue)
                        ? defaultValue
                        : null;
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                javaType = BIG_DECIMAL;
                simpleJavaType = BIG_DECIMAL;
                defaultValue = (defaultValue != null) && NumberUtils.isCreatable(defaultValue)
                        ? defaultValue
                        : null;
                break;
            case Types.INTEGER:
            case Types.SMALLINT:
                javaType = INTEGER;
                simpleJavaType = INTEGER_SIMPLE;
                isSimpleType = !isNullable;
                defaultValue = (defaultValue != null) && NumberUtils.isDigits(defaultValue)
                        ? defaultValue
                        : null;
                break;
            case Types.TIME:
                javaType = SQL_TIME;
                simpleJavaType = SQL_TIME;
                defaultValue = null;
                break;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                javaType = SQL_TIMESTAMP;
                simpleJavaType = SQL_TIMESTAMP;
                defaultValue = null;
                break;
            case Types.TINYINT:
                javaType = BYTE;
                simpleJavaType = BYTE_SIMPLE;
                isSimpleType = !isNullable;
                defaultValue = (defaultValue != null) && NumberUtils.isDigits(defaultValue)
                        ? defaultValue
                        : null;
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                javaType = BYTE_ARRAY;
                simpleJavaType = BYTE_ARRAY;
                defaultValue = null;
                break;
            case Types.ARRAY:
                javaType = Config.getCONFIG().arrayAsList
                        ? "List<" + STRING + ">"
                        : STRING + ARRAY_BRACKETS;
                simpleJavaType = javaType;
                defaultValue = null;
                break;
            default:
                LOG.warn("Undefined sql type for field [{}]", this);
                javaType = STRING;
                simpleJavaType = STRING;
                defaultValue = null;
        }
//        LOG.warn("Field: [{}]", this);
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public String getDefaultValue() {
        for (IPlugin plugin : PluginHandler.getPlugins(dbInfo)) {
            TriOptional<String> result = plugin.getDefaultValue(this);
            if (!result.isEmpty()) {
                return result.get();
            }
        }
        String fieldDefault = Config.getCONFIG().getFieldDefaults(tableName).get(name);
        if (fieldDefault != null) {
            return fieldDefault;
        }
        fieldDefault = Config.getCONFIG().getTypeDefaults(tableName).get(sqlTypeName);
        if (fieldDefault != null) {
            return fieldDefault;
        }

        String predefinedType = Config.getCONFIG().getFieldTypes(tableName).get(name);
        if (predefinedType != null) {
            return String.format(NEW_OBJECT, predefinedType);
        }

        switch (javaType) {
            case LONG:
                return defaultValue != null ? defaultValue : "0L";
            case BOOLEAN:
                return defaultValue != null ? defaultValue.toLowerCase(Locale.ROOT) : VALUE_BOOLEAN;
            case STRING:
                return defaultValue != null ? defaultValue : VALUE_STRING;
            case SQL_DATE:
                return "java.sql.Date.valueOf(java.time.LocalDate.now())";
            case BIG_INTEGER:
                return defaultValue != null ? defaultValue : "java.math.BigInteger.ZERO";
            case BIG_DECIMAL:
                return defaultValue != null ? "new java.math.BigDecimal(\"" + defaultValue + "\")" : VALUE_BIG_DECIMAL;
            case INTEGER:
                return defaultValue != null ? defaultValue : "0";
            case SQL_TIME:
                return "java.sql.Time.valueOf(java.time.LocalTime.now())";
            case SQL_TIMESTAMP:
                return "java.sql.Timestamp.from(java.time.Instant.now())";
            case LOCAL_DATE:
                return "LocalDate.now()";
            case LOCAL_DATE_TIME:
                return "LocalDateTime.now()";
            case INSTANT:
                return "Instant.now()";
            case BYTE:
                return defaultValue != null ? "(byte) " + defaultValue : "(byte) 0";
            case BYTE_ARRAY:
                return "new byte[0]";
            default:
                String test = "Map";
                if (javaType.startsWith(test)) {
                    return "new HashMap<>()";
                }
                test = "List";
                if (javaType.startsWith(test)) {
                    return "new ArrayList<>()";
                }
                test = "Set";
                if (javaType.startsWith(test)) {
                    return "new HashSet<>()";
                }
                LOG.warn("Undefined java type for get default value for field [{}]", this);
                if (sqlType == Types.ARRAY) {
                    return Config.getCONFIG().arrayAsList
                            ? "(new ArrayList<>())"
                            : NEW + javaType.replace(ARRAY_BRACKETS, "[0]");
                } else {
                    return String.format(NEW_OBJECT, javaType);
                }
        }
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).toString();
    }

}
