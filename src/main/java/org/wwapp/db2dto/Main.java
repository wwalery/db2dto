package org.wwapp.db2dto;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.wwapp.db2dto.config.Config;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class Main {

  private static final String OPTION_HELP = "h";
  private static final String OPTION_DB_URL = "d";
  private static final String OPTION_DB_URL_LONG = "url";
  private static final String OPTION_DB_URL_NAME = "dbUrl";
  private static final String OPTION_DB_USER_LONG = "user";
  private static final String OPTION_DB_USER = "u";
  private static final String OPTION_DB_USER_NAME = "dbUser";
  private static final String OPTION_DB_PASSWORD_LONG = "password";
  private static final String OPTION_DB_PASSWORD = "p";
  private static final String OPTION_DB_PASSWORD_NAME = "dbPassword";
  private static final String OPTION_DB_SCHEMA = "s";
  private static final String OPTION_DB_SCHEMA_NAME = "dbSchema";
  private static final String OPTION_DB_SCHEMA_LONG = "schema";
  private static final String OPTION_CONFIG = "c";
  private static final String OPTION_CONFIG_LONG = "config";
  private static final String OPTION_CONFIG_NAME = "configFile";

  private Main() {
    // do nothing
  }

  /** @param args the command line arguments */
  public static void main(String[] args) throws Exception {
    // configure
    Options options = new Options();
    options.addOption(
        Option.builder(OPTION_DB_URL)
            .argName(OPTION_DB_URL_NAME)
            .longOpt(OPTION_DB_URL_LONG)
            .hasArg()
            .desc("Database connection string.")
            .build());
    options.addOption(
        Option.builder(OPTION_DB_USER)
            .argName(OPTION_DB_USER_NAME)
            .longOpt(OPTION_DB_USER_LONG)
            .hasArg()
            .desc("Database user name.")
            .build());
    options.addOption(
        Option.builder(OPTION_DB_PASSWORD)
            .argName(OPTION_DB_PASSWORD_NAME)
            .longOpt(OPTION_DB_PASSWORD_LONG)
            .hasArg()
            .desc("Database user password.")
            .build());
    options.addOption(
        Option.builder(OPTION_CONFIG)
            .argName(OPTION_CONFIG_NAME)
            .longOpt(OPTION_CONFIG_LONG)
            .hasArg()
            .desc("Configuration file.")
            .build());
    options.addOption(
        Option.builder(OPTION_DB_SCHEMA)
            .argName(OPTION_DB_SCHEMA_NAME)
            .longOpt(OPTION_DB_SCHEMA_LONG)
            .hasArg()
            .desc("Database schema.")
            .build());
    options.addOption(OPTION_HELP, false, "This help");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    if (cmd.hasOption(OPTION_HELP)) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("", options);
      return;
    }
    String configFile = "db2dto.conf";
    if (cmd.hasOption(OPTION_CONFIG)) {
      configFile = cmd.getOptionValue(OPTION_CONFIG);
    }
    String configStr = Files.readString(Paths.get(configFile));
    Gson gson = new Gson();
    Config config = gson.fromJson(configStr, Config.class);
    //    Config config = new Config();
    //    String configStr = gson.toJson(config);
    //    Files.writeString(Paths.get("./db2dto.conf"), configStr);

    if (cmd.hasOption(OPTION_DB_URL)) {
      config.dbURL = cmd.getOptionValue(OPTION_DB_URL);
    }
    if (cmd.hasOption(OPTION_DB_USER)) {
      config.dbUser = cmd.getOptionValue(OPTION_DB_USER);
    }
    if (cmd.hasOption(OPTION_DB_PASSWORD)) {
      config.dbPassword = cmd.getOptionValue(OPTION_DB_PASSWORD);
    }
    Processor proc = new Processor();
    proc.setConfig(config);
    proc.execute();
  }
}
