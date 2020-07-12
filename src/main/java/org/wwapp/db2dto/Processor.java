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

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class Processor {

  private static final String METHOD_HAS_DIRTY_FIELD = "hasDirtyField";
  private static final String METHOD_IS_DIRTY = "isDirty";
  private static final String METHOD_RESET_DIRTY_FIELD = "resetDirtyField";
  private static final String METHOD_RESET_DIRTY = "resetDirty";
  private static final String METHOD_GET_FIELD_NAMES = "getFieldNames";
  private static final String FIELD_FIELD_NAMES = "fieldNames";
  private static final String FIELD_DIRTY_FIELDS = "dirtyFields";
  private static final String PERCENT = "%";
  private static final String DOUBLE_QUOTE = "\"";
  private static final String PARAM_FIELD_NAME = "fieldName";

  @Setter private Config config;

  private TypeSpec generateBaseMethods(String tableName, List<DBColumn> columns)
      throws IOException {
    List<Modifier> methodModifiers =
        tableName != null ? List.of(Modifier.PUBLIC) : List.of(Modifier.ABSTRACT, Modifier.PUBLIC);

    if (tableName == null) {
      System.out.println("Generate base interface: " + config.baseInterfaceName);
    } else {
      System.out.println("Generate class for table: " + tableName);
    }

    FieldSpec fieldFieldNames = null;
    FieldSpec fieldDirtyFields = null;
    if (tableName != null) {
      fieldFieldNames =
          FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class), FIELD_DIRTY_FIELDS)
              .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
              .initializer("new HashSet<>()")
              .build();
      fieldDirtyFields =
          FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class), FIELD_FIELD_NAMES)
              .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
              .initializer(
                  "Stream.of("
                      + columns.stream()
                          .map(it -> DOUBLE_QUOTE + it.name + DOUBLE_QUOTE)
                          .collect(Collectors.joining(",\n"))
                      + "\n.collect(Collectors.toSet())")
              .build();
    }

    MethodSpec methodHasDirtyField =
        MethodSpec.methodBuilder(METHOD_HAS_DIRTY_FIELD)
            .addModifiers(methodModifiers)
            .addParameter(String.class, PARAM_FIELD_NAME, Modifier.FINAL)
            .returns(boolean.class)
            .build();
    if (tableName != null) {
      methodHasDirtyField =
          methodHasDirtyField.toBuilder()
              .addStatement("$L.contains($L)", FIELD_DIRTY_FIELDS, PARAM_FIELD_NAME)
              .build();
    }

    MethodSpec methodIsDirty =
        MethodSpec.methodBuilder(METHOD_IS_DIRTY)
            .addModifiers(methodModifiers)
            .returns(boolean.class)
            .build();
    if (tableName != null) {
      methodIsDirty =
          methodIsDirty.toBuilder().addStatement("!$L.isEmpty()", FIELD_DIRTY_FIELDS).build();
    }

    MethodSpec methodResetDirtyField =
        MethodSpec.methodBuilder(METHOD_RESET_DIRTY_FIELD)
            .addModifiers(methodModifiers)
            .addParameter(String.class, PARAM_FIELD_NAME, Modifier.FINAL)
            .build();
    if (tableName != null) {
      methodResetDirtyField =
          methodResetDirtyField.toBuilder()
              .addStatement("$L.remove($L)", FIELD_DIRTY_FIELDS, PARAM_FIELD_NAME)
              .build();
    }

    MethodSpec methodResetDirty =
        MethodSpec.methodBuilder(METHOD_RESET_DIRTY).addModifiers(methodModifiers).build();
    if (tableName != null) {
      methodResetDirty =
          methodResetDirty.toBuilder().addStatement("$L.clear()", FIELD_DIRTY_FIELDS).build();
    }

    MethodSpec methodGetFieldNames =
        MethodSpec.methodBuilder(METHOD_GET_FIELD_NAMES)
            .addModifiers(methodModifiers)
            .returns(ParameterizedTypeName.get(Set.class, String.class))
            .build();
    if (tableName != null) {
      methodGetFieldNames =
          methodGetFieldNames.toBuilder().addStatement("return $L", FIELD_FIELD_NAMES).build();
    }

    TypeSpec baseType;
    if (tableName == null) {
      baseType = TypeSpec.interfaceBuilder(config.baseInterfaceName).build();
    } else {
      baseType =
          TypeSpec.classBuilder(config.classPrefix + tableName + config.classSuffix)
              .addSuperinterface(ClassName.get(config.javaPackageName, config.baseInterfaceName))
              .addField(fieldFieldNames)
              .addField(fieldDirtyFields)
              .build();
    }

    baseType =
        baseType.toBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addMethod(methodHasDirtyField)
            .addMethod(methodIsDirty)
            .addMethod(methodResetDirtyField)
            .addMethod(methodResetDirty)
            .addMethod(methodGetFieldNames)
            .build();
    return baseType;
  }

  private void generateForTable(String tableName, List<DBColumn> columns) throws IOException {
    TypeSpec clazz = generateBaseMethods(tableName, columns);
    JavaFile javaFile = JavaFile.builder(config.javaPackageName, clazz).build();
    javaFile.writeTo(Paths.get(config.outputDir));
  }

  public void execute() throws IOException, SQLException {
    if (config == null) {
      throw new IllegalArgumentException("config not defined");
    }
    config.check();
    Files.createDirectories(Paths.get(config.outputDir));

    TypeSpec baseInterface = generateBaseMethods(null, null);
    JavaFile javaFile = JavaFile.builder(config.javaPackageName, baseInterface).build();
    javaFile.writeTo(Paths.get(config.outputDir));

    try (Connection jdbcConnection =
        DriverManager.getConnection(config.dbURL, config.dbUser, config.dbPassword)) {
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
          String javaTableName =
              CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());
          generateForTable(javaTableName, columns);
        }
      }
    }
  }
}
