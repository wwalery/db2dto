package org.wwapp.db2dto;

import com.google.common.base.Strings;

/**
 * Common configuration.
 *
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class Config {

  // DB config
  /** URL to database, required. */
  public String dbURL;
  /** Database user, required. */
  public String dbUser;
  /** Database password, optional. */
  public String dbPassword = "";

  /**
   * Java package name for generated classes.
   *
   * <p>'dto' by default
   */
  public String javaPackageName = "dto";
  /**
   * Output directory for generated classes.
   *
   * <p>'./build/generated.dto' by default
   */
  public String outputDir = "./build/generated.dto";

  /**
   * Name of base interface for all generated classes.
   *
   * <p>'IData' by default
   */
  public String baseInterfaceName = "IData";

  /**
   * Prefix for generated class name.
   *
   * <p>empty by default
   */
  public String classPrefix = "";

  /**
   * Suffix for generated class name.
   *
   * <p>'Data' by default
   */
  public String classSuffix = "Data";

  void check() {
    if (Strings.isNullOrEmpty(dbURL)) {
      throw new IllegalArgumentException("[dbURL] not defined");
    }
    if (Strings.isNullOrEmpty(dbUser)) {
      throw new IllegalArgumentException("[dbUser] not defined");
    }
  }
}
