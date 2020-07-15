package org.wwapp.db2dto;

import com.google.common.base.CaseFormat;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import lombok.Setter;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class Processor {

  private static final String TEMPLATE_INTERFACE = "templates/interface.tpl";
  private static final String TEMPLATE_CLASS = "templates/class.tpl";
  private static final String PERCENT = "%";
  private static final char DOT = '.';
  private static final char SLASH = '/';
  private static final String CONFIG = "config";
  private static final String EXT_JAVA = ".java";

  @Setter private Config config;

  private PebbleEngine pebbleEngine;

  private void writeToDisk(String packageName, String className, String data) throws IOException {
    Path path = Paths.get(config.outputDir, packageName.replace(DOT, SLASH), className + EXT_JAVA);
    Files.createDirectories(Paths.get(config.outputDir, packageName.replace(DOT, SLASH)));
    Files.writeString(path, data);
  }

  private void generateForTable(DBTable table) throws IOException {
    PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(TEMPLATE_CLASS);
    Writer writer = new StringWriter();
    compiledTemplate.evaluate(writer, Map.of(CONFIG, config, "table", table));
    String result = writer.toString();
    writeToDisk(config.getPackageName(table.name), table.javaName, result);
  }

  public void execute() throws IOException, SQLException {
    if (config == null) {
      throw new IllegalArgumentException("config not defined");
    }
    config.check();
    Files.createDirectories(Paths.get(config.outputDir));

    pebbleEngine =
        new PebbleEngine.Builder()
            //                    .syntax(syntax)
            .loader(new ClasspathLoader())
            .cacheActive(true)
            .strictVariables(true)
            .build();
    //    pebbleStringEngine = new PebbleEngine.Builder()
    //            .syntax(syntax)
    //            .loader(new StringLoader())
    //            .cacheActive(NURSConfig.config.production)
    //            .strictVariables(true)
    //            .build();

    PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(TEMPLATE_INTERFACE);
    Writer writer = new StringWriter();
    compiledTemplate.evaluate(writer, Map.of(CONFIG, config));
    String result = writer.toString();
    writeToDisk(config.getPackageName(""), config.baseInterfaceName, result);

    try (Connection jdbcConnection =
        DriverManager.getConnection(config.dbURL, config.dbUser, config.dbPassword)) {
      DatabaseMetaData metadata = jdbcConnection.getMetaData();
      try (ResultSet rs = metadata.getTables(null, null, PERCENT, null)) {
        while (rs.next()) {
          DBTable table = new DBTable();
          table.name = rs.getString("TABLE_NAME");
          table.javaName =
              config.classPrefix
                  + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.name.toLowerCase())
                  + config.classSuffix;

          try (ResultSet rsColumns = metadata.getColumns(null, null, table.name, PERCENT)) {
            table.columns = new ArrayList<>();
            while (rsColumns.next()) {
              table.columns.add(new DBColumn(rsColumns));
            }
          }
          generateForTable(table);
        }
      }
    }
  }
}
