package org.wwapp.db2dto;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.wwapp.db2dto.config.Config;

/** @author Walery Wysotsky <dev@wysotsky.info> */
@Slf4j
public class Processor {

  private static final String TEMPLATE_INTERFACE = "interface.tpl";
  private static final String TEMPLATE_CLASS = "class.tpl";
  private static final String PERCENT = "%";
  private static final char DOT = '.';
  private static final char SLASH = '/';
  private static final String CONFIG = "config";
  private static final String EXT_JAVA = ".java";
  private static final String SPACE = " ";
  private static final String TYPE_TABLE = "TABLE";

  @Setter private Config config;

  private PebbleEngine pebbleEngine;

  @Getter private Map<String, DBTable> tables = new HashMap<>();

  private void writeToDisk(String packageName, String className, String data) throws IOException {
    Path path =
        Paths.get(config.sourceOutputDir, packageName.replace(DOT, SLASH), className + EXT_JAVA);
    Files.createDirectories(Paths.get(config.sourceOutputDir, packageName.replace(DOT, SLASH)));
    Files.writeString(path, data);
  }

  private void generateForTable(DBTable table) throws IOException {
    PebbleTemplate compiledTemplate =
        pebbleEngine.getTemplate(config.templateDir + SLASH + TEMPLATE_CLASS);
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

    pebbleEngine =
        new PebbleEngine.Builder()
            //                    .syntax(syntax)
            .cacheActive(true)
            .strictVariables(true)
            .build();
    //    pebbleStringEngine = new PebbleEngine.Builder()
    //            .syntax(syntax)
    //            .loader(new StringLoader())
    //            .cacheActive(NURSConfig.config.production)
    //            .strictVariables(true)
    //            .build();

    PebbleTemplate compiledTemplate =
        pebbleEngine.getTemplate(config.templateDir + SLASH + TEMPLATE_INTERFACE);
    Writer writer = new StringWriter();
    compiledTemplate.evaluate(writer, Map.of(CONFIG, config));
    String result = writer.toString();
    writeToDisk(config.getPackageName(""), config.baseInterfaceName, result);

    try (Connection jdbcConnection =
        DriverManager.getConnection(config.dbURL, config.dbUser, config.dbPassword)) {
      DatabaseMetaData metadata = jdbcConnection.getMetaData();
      try (ResultSet rs = metadata.getTables(null, config.dbSchema, PERCENT, null)) {
        while (rs.next()) {
          DBTable table = new DBTable(rs);
          LOG.debug(table.toString());
          if (!TYPE_TABLE.equals(table.type)) {
            continue;
          }

          try (ResultSet rsColumns =
              metadata.getColumns(null, config.dbSchema, table.realName, PERCENT)) {
            table.columns = new ArrayList<>();
            while (rsColumns.next()) {
              table.columns.add(new DBColumn(rsColumns));
            }
          }
          tables.put(table.name, table);
          generateForTable(table);
        }
      }
    }

    if (config.compile) {
      compile();
    }
  }

  private void compile() throws Exception {
    // compile
    List<String> options =
        List.of(
            // set compiler's classpath to be same as the runtime's
            "-classpath", System.getProperty("java.class.path"), "-d", config.classOutputDir);
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    Files.createDirectories(Paths.get(config.classOutputDir));

    List<Path> filesToCompile =
        Files.walk(Paths.get(config.sourceOutputDir))
            .filter(it -> it.toFile().isFile())
            .collect(Collectors.toList());
    Iterable<? extends JavaFileObject> fileObjects =
        fileManager.getJavaFileObjectsFromPaths(filesToCompile);

    Callable<Boolean> task = compiler.getTask(null, fileManager, null, options, null, fileObjects);
    if (!task.call()) {
      throw new Exception("Compilation failed");
    }

    // pack
    int baseLen = config.classOutputDir.length();
    String filesToPack =
        Files.walk(Paths.get(config.classOutputDir))
            .filter(it -> it.toFile().isFile())
            .map(
                it -> " -C " + config.classOutputDir + SPACE + it.toString().substring(baseLen + 1))
            .collect(Collectors.joining(SPACE));

    String cmd = "jar --create --file " + config.jarPath + SPACE + filesToPack;
    Process process = Runtime.getRuntime().exec(cmd);

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
