package org.wwapp.db2dto;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class Main {

  private static final String OPTION_HELP = "h";
  private static final String OPTION_DB_URL = "url";
  private static final String OPTION_DB_USER = "user";
  private static final String OPTION_DB_PASSWORD = "password";
  private static final String OPTION_DB_URL_NAME = "dbUrl";
  private static final String OPTION_DB_USER_NAME = "dbUser";
  private static final String OPTION_DB_PASSWORD_NAME = "dbPassword";

  private Main() {
    // do nothing
  }

  /** @param args the command line arguments */
  public static void main(String[] args) throws ParseException, IOException, SQLException {
    // configure
    Options options = new Options();
    options.addOption(
        Option.builder(OPTION_DB_URL)
            .argName(OPTION_DB_URL_NAME)
            .hasArg()
            .desc("Database connection string.")
            .build());
    options.addOption(
        Option.builder(OPTION_DB_USER)
            .argName(OPTION_DB_USER_NAME)
            .hasArg()
            .desc("Database user name.")
            .build());
    options.addOption(
        Option.builder(OPTION_DB_PASSWORD)
            .argName(OPTION_DB_PASSWORD_NAME)
            .hasArg()
            .desc("Database user password.")
            .build());
    options.addOption(OPTION_HELP, false, "This help");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    if (cmd.hasOption(OPTION_HELP)) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("", options);
      return;
    }
    Config config = new Config();
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
