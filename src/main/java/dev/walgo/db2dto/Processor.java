package dev.walgo.db2dto;

import dev.walgo.db2dto.config.Config;
import dev.walgo.db2dto.config.TableConfig;
import dev.walgo.db2dto.plugin.PluginHandler;
import dev.walgo.walib.db.ColumnInfo;
import dev.walgo.walib.db.DBInfo;
import dev.walgo.walib.db.TableInfo;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    private static final String TEMPLATE_INTERFACE = "interface.tpl";
    private static final String TEMPLATE_CLASS = "class.tpl";
    private static final String PERCENT = "%";
    private static final char DOT = '.';
    private static final char SLASH = '/';
    private static final String CONFIG = "config";
    private static final String EXT_JAVA = ".java";
    private static final String SPACE = " ";
    private static final String TYPE_TABLE = "TABLE";

    private Config config;

    private PebbleEngine pebbleEngine;

    private final Map<String, DBTable> tables = new HashMap<>();

    public Map<String, DBTable> getTables() {
        return tables;
    }

    public void setConfig(Config config) {
        PluginHandler.clearPlugins();
        this.config = config;
    }

    private void writeToDisk(String packageName, String className, String data) throws IOException {
        Path path = Paths.get(config.sourceOutputDir, packageName.replace(DOT, SLASH), className + EXT_JAVA);
        Files.createDirectories(Paths.get(config.sourceOutputDir, packageName.replace(DOT, SLASH)));
        Files.writeString(path, data);
    }

    private void generateForTable(DBTable table) throws IOException {
        PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(config.templateDir + SLASH + TEMPLATE_CLASS);
        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, Map.of(CONFIG, config, "table", table));
        String result = writer.toString();
        writeToDisk(config.getPackageName(table.name), table.javaName, result);
    }

    public void execute() throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("config not defined");
        }
        config.check();
        Files.createDirectories(Paths.get(config.sourceOutputDir));

        pebbleEngine = new PebbleEngine.Builder()
                // .syntax(syntax)
                .cacheActive(true)
                .strictVariables(true)
                .build();
        // pebbleStringEngine = new PebbleEngine.Builder()
        // .syntax(syntax)
        // .loader(new StringLoader())
        // .cacheActive(NURSConfig.config.production)
        // .strictVariables(true)
        // .build();

        if (StringUtils.isNotEmpty(config.baseInterfaceName)) {
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(config.templateDir + SLASH + TEMPLATE_INTERFACE);
            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, Map.of(CONFIG, config));
            String result = writer.toString();
            writeToDisk(config.getPackageName(""), config.baseInterfaceName, result);
        }

        try (Connection jdbcConnection = DriverManager.getConnection(config.dbURL, config.dbUser, config.dbPassword)) {
            DBInfo info = new DBInfo(jdbcConnection, null, config.dbSchema, PERCENT);

            // DatabaseMetaData metadata = jdbcConnection.getMetaData();
            // try (ResultSet rs = metadata.getTables(null, config.dbSchema, PERCENT, null)) {
            for (TableInfo dbTable : info.getTables()) {
                DBTable table = new DBTable(dbTable);
                LOG.debug(table.toString());
                if (!TYPE_TABLE.equals(table.type)) {
                    continue;
                }

//                ResultSetMetaData tableMeta = dbTable.getMetaData();
//                for (int idx = 1; idx <= tableMeta.getColumnCount(); idx++) {
//                    LOG.info("{} = {}", tableMeta.getColumnName(idx), tableMeta.getColumnClassName(idx));
//                }

                table.columns = new ArrayList<>();
                for (ColumnInfo dbColumn : dbTable.getFields().values()) {
                    // try (ResultSet rsColumns = metadata.getColumns(null,
                    // config.dbSchema, table.realName, PERCENT)) {
                    // while (rsColumns.next()) {
                    DBColumn column = new DBColumn(table.realName, dbColumn, info);
                    column.fillJavaType();
                    table.columns.add(column);
                    // }
                }

                TableConfig.ColumnOrder order = config.getColumnsOrder(table.name);
                switch (order) {
                    case ALPHA:
                        table.columns.sort(Comparator.comparing(it -> it.name));
                        break;
                    case TABLE:
                        table.columns.sort(Comparator.comparing(it -> it.order));
                        break;
                    default:
                        throw new RuntimeException("Indefined sort order: [%s]".formatted(order));
                }
                tables.put(table.name, table);
                generateForTable(table);
            }
        }
        // }

        if (config.compile) {
            compile();
        }
    }

    private void compile() throws Exception {
        // compile
        List<String> options = List.of(
                // set compiler's classpath to be same as the runtime's
                "-classpath", System.getProperty("java.class.path"), "-d", config.classOutputDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Files.createDirectories(Paths.get(config.classOutputDir));

        List<Path> filesToCompile = Files.walk(Paths.get(config.sourceOutputDir))
                .filter(it -> it.toFile().isFile())
                .collect(Collectors.toList());
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromPaths(filesToCompile);

        Callable<Boolean> task = compiler.getTask(null, fileManager, null, options, null, fileObjects);
        if (!task.call()) {
            throw new Exception("Compilation failed");
        }

        // pack
        int baseLen = config.classOutputDir.length();
        List<String> filesToPack = Files.walk(Paths.get(config.classOutputDir))
                .filter(it -> it.toFile().isFile())
                .flatMap(
                        it -> Arrays.stream(
                                new String[] { "-C", config.classOutputDir, it.toString().substring(baseLen + 1) }))
                .toList();

        List<String> cmdStart = List.of("jar", "--create", "--file=" + config.jarPath);
        List<String> cmd = new ArrayList<>(cmdStart);
        cmd.addAll(filesToPack);
        LOG.debug("Execute command: {}", cmd);
        Process process = Runtime.getRuntime().exec(cmd.toArray(String[]::new));

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            LOG.info(line);
        }
        while ((line = errReader.readLine()) != null) {
            LOG.error(line);
        }

        int exitVal = process.waitFor();
        if (exitVal != 0) {
            throw new Exception("Error in JAR creation");
        }
    }
}
