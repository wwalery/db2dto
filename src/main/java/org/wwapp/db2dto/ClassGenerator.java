package org.wwapp.db2dto;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 *
 * @author Walery Wysotsky <dev@wysotsky.info>
 */
public class ClassGenerator {

  static final String FIELD_CHANGED_FIELDS = "changedFields";
  static final String FIELD_FIELD_NAMES = "fieldNames";

  private Config config;
  private String tableName;
  private String javaClassName;
  private TypeSpec model;

  public ClassGenerator(Config config) {
    this.config = config;
  }

// GET/SET changed
  
  private MethodSpec addChangedSetter(DBColumn column) {
    MethodSpec method
            = MethodSpec.methodBuilder("set" + column.javaPropertyName + "Changed")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get("", model.name))
                    .addStatement("$L.add($S)", FIELD_CHANGED_FIELDS, column.name)
                    .addStatement("return this")
                    .build();
    return method;
  }

  private MethodSpec addChangedGetter(DBColumn column) {
    MethodSpec method
            = MethodSpec.methodBuilder("is" + column.javaPropertyName + "Changed")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boolean.class)
                    .addStatement("this.$L.contains($S)", FIELD_CHANGED_FIELDS, column.name)
                    .build();
    return method;
  }
  
// GET/SET boolean
  private MethodSpec addBooleanSetter(DBColumn column) {
    final String paramName = "newValue";
    MethodSpec method
            = MethodSpec.methodBuilder("set" + column.javaPropertyName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(boolean.class, paramName, Modifier.FINAL)
                    .returns(ClassName.get("", model.name))
                    .addStatement("Byte value = Byte.valueOf($L ?  (byte) 1 : (byte) 0))", 
                            paramName)
                    .beginControlFlow("if (!java.util.Objects.equals(this.$L, $L))",
                            column.javaFieldName, paramName)
                    .addStatement("this.$L.add($S)", FIELD_CHANGED_FIELDS, column.name)
                    .endControlFlow()
                    .addStatement("return this")
                    .build();
    return method;
  }

  private MethodSpec addBooleanGetter(DBColumn column) {
    MethodSpec method
            = MethodSpec.methodBuilder("get" + column.javaPropertyName + "Bool")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boolean.class)
                    .addStatement("return this.$L == 1", column.javaFieldName)
                    .build();
    return method;
  }

  

// GET/SET Enum
  private MethodSpec addEnumSetter(DBColumn column, Enum<?> enumType) {
    final String paramName = "newValue";
    MethodSpec method
            = MethodSpec.methodBuilder("set" + column.javaPropertyName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(enumType.getClass(), paramName, Modifier.FINAL)
                    .returns(ClassName.get("", model.name))
                    .beginControlFlow("if (!$L.name().equals(this.$L))",
                            paramName, column.javaFieldName)
                    .addStatement("this.$L = $L.name())", column.javaFieldName, paramName)
                    .addStatement("this.$L.add($S)", FIELD_CHANGED_FIELDS, column.name)
                    .endControlFlow()
                    .addStatement("return this")
                    .build();
    return method;
  }

  private MethodSpec addEnumGetter(DBColumn column, Enum<?> enumType) {
    MethodSpec method
            = MethodSpec.methodBuilder("get" + column.javaPropertyName + "Enum")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(enumType.getClass())
                    .addStatement("this.$L == null ? null : $L.valueOf(this.$L)", 
                            column.javaFieldName, enumType.getClass(), column.javaFieldName)
                    .build();
    return method;
  }
  
  
// GET/SET standard  
  private MethodSpec addSetter(DBColumn column) {
    final String paramName = "newValue";
    MethodSpec method
            = MethodSpec.methodBuilder("set" + column.javaPropertyName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.bestGuess(column.javaType), paramName, Modifier.FINAL)
                    .returns(ClassName.get("", model.name))
                    .beginControlFlow("if (!java.util.Objects.equals(this.$L, $L))",
                            column.javaFieldName, paramName)
                    .addStatement("this.$L = $L", column.javaFieldName, paramName)
                    .addStatement("this.$L.add($S)", FIELD_CHANGED_FIELDS, column.name)
                    .endControlFlow()
                    .addStatement("return this")
                    .build();
    return method;
  }

  private MethodSpec addSetterNotNull(DBColumn column) {
    final String paramName = "newValue";
    MethodSpec method
            = MethodSpec.methodBuilder("set" + column.javaPropertyName + "NotNull")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.bestGuess(column.javaType), paramName, Modifier.FINAL)
                    .returns(ClassName.get("", model.name))
                    .beginControlFlow("if (!java.util.Objects.equals(this.$L, $L)  && ($L != null))",
                            column.javaFieldName, paramName, paramName)
                    .addStatement("this.$L = $L", column.javaFieldName, paramName)
                    .addStatement("this.$L.add($S)", FIELD_CHANGED_FIELDS, column.name)
                    .endControlFlow()
                    .addStatement("return this")
                    .build();
    return method;
  }

  
  private MethodSpec addGetter(DBColumn column) {
    MethodSpec method
            = MethodSpec.methodBuilder("get" + column.javaPropertyName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess(column.javaType))
                    .addStatement("return this.$L", column.javaFieldName)
                    .build();
    return method;
  }
  
  @Override
  public void serialize(EntityType model, SerializerConfig serializerConfig, CodeWriter writer)
          throws IOException {
    String simpleName = model.getSimpleName();
    //    System.err.println("SimpleName: " + simpleName);

    // package
    if (!model.getPackageName().isEmpty()) {
      writer.packageDecl(model.getPackageName());
    }

    // imports
    Set<String> importedClasses = getAnnotationTypes(model);
    List<Type> classInterfaces = interfaces.get(simpleName);
    if (classInterfaces == null) {
      classInterfaces = new ArrayList<>();
    }
    for (Type iface : classInterfaces) {
      importedClasses.add(iface.getFullName());
    }
    //    importedClasses.add(JsonIgnore.class.getName());
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("1.8.")) {
      importedClasses.add("javax.annotation.Generated");
    } else if (javaVersion.startsWith("11.")) {
      importedClasses.add("javax.annotation.processing.Generated");
    } else {
      throw new IBSException("error", "Unsupported java version: " + javaVersion);
    }
    if (model.hasLists()) {
      importedClasses.add(List.class.getName());
    }
    if (model.hasCollections()) {
      importedClasses.add(Collection.class.getName());
    }

    // import Set and HashSet since we need them for internal purposes
    // importedClasses.add(Set.class.getName());
    // importedClasses.add(HashSet.class.getName());
    if (model.hasMaps()) {
      importedClasses.add(Map.class.getName());
    }
    if (addToString && model.hasArrays()) {
      importedClasses.add(Arrays.class.getName());
    }
    writer.importClasses(importedClasses.toArray(new String[importedClasses.size()]));

    // javadoc
    writer.javadoc(simpleName + javadocSuffix);

    // header
    for (Annotation annotation : model.getAnnotations()) {
      writer.annotation(annotation);
    }

    writer.line("@Generated(\"", getClass().getName(), "\")");

    Type superType = new ClassType(AbstractDBBean.class);
    if (!classInterfaces.isEmpty()) {
      Type[] ifaces = classInterfaces.toArray(new Type[classInterfaces.size()]);
      writer.beginClass(model, superType, ifaces);
    } else {
      writer.beginClass(model, superType);
    }

    bodyStart(model, writer);

    // public empty constructor
    writer.beginConstructor();
    writer.end();

    if (addFullConstructor) {
      addFullConstructor(model, writer);
    }
    addCopyConstructor(model, writer);

    // write service field used to hold updated field names
    //    writer.privateFinal(typeFactory.get(Set.class, String.class), "dirtyFields", "new
    // HashSet<>()");
    // fields
    for (Property property : model.getProperties()) {
      if (propertyAnnotations) {
        for (Annotation annotation : property.getAnnotations()) {
          writer.annotation(annotation);
        }
      }
      writer.privateField(property.getType(), property.getEscapedName());
    }

    Map<String, Type> addFields = additionalFields.get(simpleName);
    if (addFields != null) {
      for (Map.Entry<String, Type> field : addFields.entrySet()) {
        writer.privateField(field.getValue(), field.getKey());
      }
    }

    // write helper method used to check if field is dirty
    //    writer.javadoc("Check if field with provided name was changed after new instance
    // creation");
    //    writer.annotation(JsonIgnore.class);
    //    writer.beginPublicMethod(Types.BOOLEAN_P, "hasChangedField", new Parameter("name",
    // Types.STRING));
    //    writer.line("return dirtyFields.contains(name);");
    //    writer.end();
    //    writer.javadoc("Check if object was changed after new instance creation");
    //    writer.annotation(JsonIgnore.class);
    //    writer.beginPublicMethod(Types.BOOLEAN_P, "isChanged");
    //    writer.line("return !dirtyFields.isEmpty();");
    //    writer.end();
    //    writer.javadoc("Clear dirty fields.");
    //    writer.beginPublicMethod(Types.VOID, "resetChangedFields");
    //    writer.line("dirtyFields.clear();");
    //    writer.end();
    // accessors
    for (Property property : model.getProperties()) {
      addGetterAndSetter(model, property.getEscapedName(), property.getType(), writer);
    }

    if (addFields != null) {
      for (Map.Entry<String, Type> field : addFields.entrySet()) {
        addGetterAndSetter(model, field.getKey(), field.getValue(), writer);
      }
    }

    if (addToString) {
      addToString(model, writer);
    }

    bodyEnd(model, writer);

    writer.end();
  }

  protected void addFullConstructor(EntityType model, CodeWriter writer) throws IOException {
    // full constructor
    writer.beginConstructor(model.getProperties(), propertyToParameter);
    for (Property property : model.getProperties()) {
      writer.line("this.", property.getEscapedName(), " = ", property.getEscapedName(), ";");
    }
    writer.end();
  }

  protected void addCopyConstructor(EntityType model, CodeWriter writer) throws IOException {
    // Copy constructor
    //    System.out.println(model.getModifiedSimpleName() + " = " +
    // model.getInnerType().getFullName());
    String name = "cpy"; // model.getModifiedSimpleName();
    Parameter parameter = new Parameter(name, model.getInnerType());
    writer.beginConstructor(parameter);
    for (Property property : model.getProperties()) {
      if (!READ_ONLY_FIELDS.contains(property.getName())) {

        String getter = String.format("%s.get%s()", name, BeanUtils.capitalize(property.getName()));
        writer.line(
                String.format("this.set%s(%s);", BeanUtils.capitalize(property.getName()), getter));
      }
    }
    Map<String, Type> addFields = additionalFields.get(model.getSimpleName());
    if (addFields != null) {
      for (Map.Entry<String, Type> field : addFields.entrySet()) {
        String getter = String.format("%s.get%s()", name, BeanUtils.capitalize(field.getKey()));
        if (field.getValue().isPrimitive()) {
          writer.line("this.", "set" + BeanUtils.capitalize(field.getKey()) + "(" + getter + ");");
        } else if (field.getValue().getComponentType() != null) {
          writer.line(
                  "this.",
                  "set"
                  + BeanUtils.capitalize(field.getKey())
                  + "("
                  + getter
                  + " != null ? "
                  + getter
                  + ".clone() : null);");
        } else {
          writer.line("if (", getter, " != null) {");
          writer.line(
                  "  this.",
                  field.getKey(),
                  " = new ",
                  field.getValue().getFullName(),
                  "(",
                  getter,
                  ");");
          writer.line("}");
        }
      }
    }
    writer.end();
  }

  private void itemToString(StringBuilder builder, String name, Type type) {
    if (builder.length() > 0) {
      builder.append(" + \", ");
    } else {
      builder.append("\"");
    }
    builder.append(name + " = \" + ");
    if (type.getCategory() == TypeCategory.ARRAY) {
      builder.append("Arrays.toString(" + name + ")");
    } else {
      builder.append(name);
    }
  }

  protected void addToString(EntityType model, CodeWriter writer) throws IOException {
    writer.line("@Override");
    writer.beginPublicMethod(Types.STRING, "toString");
    StringBuilder builder = new StringBuilder();
    for (Property property : model.getProperties()) {
      String propertyName = property.getEscapedName();
      itemToString(builder, propertyName, property.getType());
    }
    Map<String, Type> addFields = additionalFields.get(model.getSimpleName());
    if (addFields != null) {
      for (Map.Entry<String, Type> field : addFields.entrySet()) {
        if ((field.getValue().getComponentType() != null)
                && ("byte".equals(field.getValue().getComponentType().getSimpleName()))) {
          continue;
        }
        itemToString(builder, field.getKey(), field.getValue());
      }
    }
    writer.line(" return ", builder.toString(), ";");
    writer.end();
  }

  protected void addFieldNames(EntityType model, CodeWriter writer) throws IOException {
    writer.line("public enum Fields {");
    writer.line();
    Iterator<Property> iterator = model.getProperties().iterator();
    while (iterator.hasNext()) {
      Property property = iterator.next();
      String propertyName = property.getEscapedName();
      String constName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, propertyName);
      writer.beginLine(String.format("  %s(\"%s\")", constName, propertyName));
      writer.append(iterator.hasNext() ? ',' : ';');
      writer.append('\n');
    }
    writer.line();

    writer.privateField(typeFactory.get(String.class), "fieldName");
    writer.beginPublicMethod(typeFactory.get(String.class), "getFieldName");
    writer.line("return fieldName;");
    writer.end();
    writer.line();
    //    Parameter parameter = new Parameter("fieldName", typeFactory.get(String.class));
    writer.line("  Fields(final String fieldName) {");
    writer.line("    this.fieldName = fieldName;");
    writer.line("  }");
    writer.line("}");
    writer.line();
    //    writer.end();
  }


  public TypeSpec generate(String tableName, TypeSpec model, List<DBColumn> columns) {
    this.tableName = tableName;
    this.model = model;
    TypeSpec.Builder builder = model.toBuilder();
    Set<Class<?>> interfaces = config.getInterfaces(tableName);
    for (Class<?> intf : interfaces) {
      builder = builder.addSuperinterface(intf);
    }
    
    for (DBColumn column : columns) {
      builder = builder.addField(ClassName.bestGuess(column.javaType), column.javaFieldName, 
              Modifier.PRIVATE);
    }
    
    for (DBColumn column : columns) {
      builder = builder.addMethod(addGetter(column));
      Enum<?> enumClass = config.getEnums(tableName).get(column.name);
      if (enumClass != null) {
        builder = builder.addMethod(addEnumGetter(column, enumClass));
      }
      if (!config.getReadOnlyFields().contains(column.name)) {
        builder = builder.addMethod(addChangedGetter(column))
                .addMethod(addChangedSetter(column))
                .addMethod(addSetter(column))
                .addMethod(addSetterNotNull(column));
        if (enumClass != null) {
          builder = builder.addMethod(addEnumSetter(column, enumClass));
        }
      }
    }
    return builder.build();
  }

  
  
  
}
