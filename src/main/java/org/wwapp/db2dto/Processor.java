package org.wwapp.db2dto;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import lombok.Setter;

/**
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class Processor {

  private static final String METHOD_HAS_CHANGED_FIELD = "hasChangedField";
  private static final String METHOD_IS_CHANGED = "isChanged";
  private static final String METHOD_RESET_CHANGED_FIELD = "resetChangedField";
  private static final String METHOD_RESET_CHANGED = "resetChanged";
  private static final String METHOD_GET_FIELD_NAMES = "getFieldNames";
  private static final String PERCENT = "%";
  private static final String DOUBLE_QUOTE = "\"";
  private static final String PARAM_FIELD_NAME = "fieldName";

  @Setter
  private Config config;

  private TypeSpec generateBaseMethods(String tableName, List<DBColumn> columns)
          throws IOException {
    List<Modifier> methodModifiers
            = tableName != null ? List.of(Modifier.PUBLIC) : List.of(Modifier.ABSTRACT, Modifier.PUBLIC);

    if (tableName == null) {
      System.out.println("Generate base interface: " + config.baseInterfaceName);
    } else {
      System.out.println("Generate class for table: " + tableName);
    }

    FieldSpec fieldFieldNames = null;
    FieldSpec fieldChangedFields = null;
    if (tableName != null) {
      fieldFieldNames
              = FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class), ClassGenerator.FIELD_CHANGED_FIELDS)
                      .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                      .initializer("new HashSet<>()")
                      .build();
      fieldChangedFields
              = FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class), ClassGenerator.FIELD_FIELD_NAMES)
                      .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                      .initializer(
                              "Stream.of("
                              + columns.stream()
                                      .map(it -> DOUBLE_QUOTE + it.name + DOUBLE_QUOTE)
                                      .collect(Collectors.joining(",\n"))
                              + "\n.collect(Collectors.toSet())")
                      .build();
    }

    MethodSpec methodHasChangedField
            = MethodSpec.methodBuilder(METHOD_HAS_CHANGED_FIELD)
                    .addModifiers(methodModifiers)
                    .addParameter(String.class, PARAM_FIELD_NAME, Modifier.FINAL)
                    .returns(boolean.class)
                    .build();
    if (tableName != null) {
      methodHasChangedField
              = methodHasChangedField.toBuilder()
                      .addStatement("$L.contains($L)", ClassGenerator.FIELD_CHANGED_FIELDS, PARAM_FIELD_NAME)
                      .build();
    }

    MethodSpec methodIsChanged
            = MethodSpec.methodBuilder(METHOD_IS_CHANGED)
                    .addModifiers(methodModifiers)
                    .returns(boolean.class)
                    .build();
    if (tableName != null) {
      methodIsChanged
              = methodIsChanged.toBuilder().addStatement("!$L.isEmpty()", ClassGenerator.FIELD_CHANGED_FIELDS).build();
    }

    MethodSpec methodResetChangedField
            = MethodSpec.methodBuilder(METHOD_RESET_CHANGED_FIELD)
                    .addModifiers(methodModifiers)
                    .addParameter(String.class, PARAM_FIELD_NAME, Modifier.FINAL)
                    .build();
    if (tableName != null) {
      methodResetChangedField
              = methodResetChangedField.toBuilder()
                      .addStatement("$L.remove($L)", ClassGenerator.FIELD_CHANGED_FIELDS, PARAM_FIELD_NAME)
                      .build();
    }

    MethodSpec methodResetChanged
            = MethodSpec.methodBuilder(METHOD_RESET_CHANGED).addModifiers(methodModifiers).build();
    if (tableName != null) {
      methodResetChanged = methodResetChanged.toBuilder()
              .addStatement("$L.clear()", ClassGenerator.FIELD_CHANGED_FIELDS)
              .build();
    }

    MethodSpec methodGetFieldNames
            = MethodSpec.methodBuilder(METHOD_GET_FIELD_NAMES)
                    .addModifiers(methodModifiers)
                    .returns(ParameterizedTypeName.get(Set.class, String.class))
                    .build();
    if (tableName != null) {
      methodGetFieldNames = methodGetFieldNames.toBuilder()
              .addStatement("return $L", ClassGenerator.FIELD_FIELD_NAMES)
              .build();
    }

    TypeSpec baseType;
    if (tableName == null) {
      baseType = TypeSpec.interfaceBuilder(config.baseInterfaceName).build();
    } else {
      String classBaseName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
              tableName.toLowerCase());

      baseType
              = TypeSpec.classBuilder(config.classPrefix + classBaseName + config.classSuffix)
                      .addSuperinterface(ClassName.get(config.getPackageName(tableName),
                              config.baseInterfaceName))
                      .addField(fieldFieldNames)
                      .addField(fieldChangedFields)
                      .build();
    }

    baseType
            = baseType.toBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodHasChangedField)
                    .addMethod(methodIsChanged)
                    .addMethod(methodResetChangedField)
                    .addMethod(methodResetChanged)
                    .addMethod(methodGetFieldNames)
                    .build();
    return baseType;
  }

  private void generateForTable(String tableName, List<DBColumn> columns) throws IOException {
    TypeSpec clazz = generateBaseMethods(tableName, columns);
    ClassGenerator generator = new ClassGenerator(config);
    clazz = generator.generate(tableName, clazz, columns);
    JavaFile javaFile = JavaFile.builder(config.getPackageName(tableName), clazz).build();
    javaFile.writeTo(Paths.get(config.outputDir));
  }

  public void execute() throws IOException, SQLException {
    if (config == null) {
      throw new IllegalArgumentException("config not defined");
    }
    config.check();
    Files.createDirectories(Paths.get(config.outputDir));

    TypeSpec baseInterface = generateBaseMethods(null, null);
    JavaFile javaFile = JavaFile.builder(config.getPackageName(""), baseInterface).build();
    javaFile.writeTo(Paths.get(config.outputDir));

    try (Connection jdbcConnection
            = DriverManager.getConnection(config.dbURL, config.dbUser, config.dbPassword)) {
      DatabaseMetaData metadata = jdbcConnection.getMetaData();
      try (ResultSet rs = metadata.getTables(null, null, PERCENT, null)) {
        while (rs.next()) {
          String tableName = rs.getString("TABLE_NAME");
          List<DBColumn> columns;
          try (ResultSet rsColumns = metadata.getColumns(null, null, tableName, PERCENT)) {
            columns = new ArrayList<>();
            while (rsColumns.next()) {
              columns.add(new DBColumn(rsColumns));
            }
          }
          generateForTable(tableName, columns);
        }
      }
    }
  }
}
