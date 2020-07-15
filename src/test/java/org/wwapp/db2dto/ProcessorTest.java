package org.wwapp.db2dto;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wwapp.db2dto.config.Config;

/** @author Walery Wysotsky <dev@wysotsky.info> */
@Slf4j
public class ProcessorTest {

  private static final String DB_USER = "sa";
  private static final String DB_URL = "jdbc:hsqldb:testdb";

  private Connection conn;

  @Before
  public void before() throws SQLException, IOException, SqlToolError {
    conn = DriverManager.getConnection(DB_URL, DB_USER, "");
    try (InputStream inputStream = new FileInputStream("src/test/resources/db/create_db.sql")) {
      SqlFile sqlFile =
          new SqlFile(
              new InputStreamReader(inputStream),
              "init",
              System.out,
              "UTF-8",
              false,
              new File("."));
      sqlFile.setConnection(conn);
      sqlFile.execute();
    }
  }

  @After
  public void after() throws SQLException, IOException {
    conn.close();
    Files.delete(Paths.get("testdb.log"));
    Files.delete(Paths.get("testdb.properties"));
    Files.delete(Paths.get("testdb.script"));
  }

  /** Test of execute method, of class Processor. */
  @Test
  public void testExecute() throws Exception {
    String configStr = Files.readString(Paths.get("./examples/db2dto.conf"));
    Gson gson = new Gson();
    Config config = gson.fromJson(configStr, Config.class);
    config.dbURL = DB_URL;
    config.dbUser = DB_USER;
    Processor instance = new Processor();
    instance.setConfig(config);
    instance.execute();
  }
}
