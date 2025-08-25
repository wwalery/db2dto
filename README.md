# DB2DTO

[![Maven Central](https://img.shields.io/maven-central/v/dev.walgo/db2dto)](https://central.sonatype.com/artifact/dev.walgo/db2dto)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)

Flexible DTO Java classes generator that creates advanced DTO classes from database metadata with extensive customization options.

## Features

- 🏗️ **Database-driven**: Generate DTOs directly from database schema metadata
- 🔧 **Highly Customizable**: Extensive configuration options for fields, types, and behavior
- 📝 **Template-based**: Uses Pebble templates with support for custom templates
- 🔌 **Plugin System**: Extensible plugin architecture for database-specific handling
- 📊 **Change Tracking**: Built-in field change tracking capabilities
- 🎯 **Multiple Templates**: Standard DTOs and Panache entity support
- 🏭 **Build Integration**: Gradle and Maven integration support
- 📦 **Auto-compilation**: Optional compilation and JAR creation

## Quick Start

1. **Create configuration file** `db2dto.conf`:
```json
{
  "dbURL": "jdbc:postgresql://localhost/mydb",
  "dbUser": "username",
  "dbPassword": "password",
  "dbSchema": "public",
  "sourceOutputDir": "./generated",
  "common": {
    "packageName": "com.example.dto",
    "classSuffix": "Data"
  }
}
```

2. **Run the generator**:
```bash
java -jar db2dto-1.20.0.jar -c db2dto.conf
```

3. **Use generated DTOs**:
```java
UserData user = new UserData()
    .setName("John Doe")
    .setEmail("john@example.com");

// Check if field changed
if (user.isNameChanged()) {
    // Handle change
}
```

## Installation

### Maven
```xml
<dependency>
    <groupId>dev.walgo</groupId>
    <artifactId>db2dto</artifactId>
    <version>1.20.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'dev.walgo:db2dto:1.20.0'
```

### Direct Download
Download the latest JAR from [Maven Central](https://central.sonatype.com/artifact/dev.walgo/db2dto) or build from source.

## Configuration

### Basic Configuration

```json
{
  "dbURL": "jdbc:postgresql://localhost/mydb",
  "dbUser": "username",
  "dbPassword": "password",
  "dbSchema": "public",
  "templateDir": "templates",
  "sourceOutputDir": "./generated",
  "baseInterfaceName": "IData",
  "compile": true,
  "classOutputDir": "build/classes",
  "jarPath": "build/myapp-dto.jar"
}
```

### Global Settings

Configure common settings for all generated classes:

```json
{
  "common": {
    "classPrefix": "",
    "classSuffix": "Data",
    "packageName": "com.example.dto",
    "columnsOrder": "TABLE",
    "useDefaults": true,
    "readOnlyFields": ["created_at", "updated_at"]
  }
}
```

**Options:**
- `classPrefix/classSuffix`: Add prefix/suffix to generated class names
- `packageName`: Java package for generated classes
- `columnsOrder`: generate columns in this order:
    - `ALPHA`: columns sorted by name
    - `TABLE`: order as in table
- `useDefaults`: Use database default values as field initializers
- `readOnlyFields`: Fields without setters (global)

### SQL Type Mapping

Override default SQL-to-Java type mappings:

```json
{
  "sqlTypes": {
    "cidr": "String",
    "_cidr": "String[]",
    "_text": "List<String>",
    "jsonb": "JsonNode"
  }
}
```

### Table-Specific Configuration

Customize individual tables:

```json
{
  "tables": {
    "users": {
      "packageName": "com.example.dto.user",
      "interfaces": ["com.example.Auditable"],
      "fieldNames": {
        "usr_name": "username"
      },
      "additionalFields": {
        "fullName": "String",
        "roles": "Set<String>",
        "metadata": "Map<String, Object>"
      },
      "enumFields": {
        "status": "com.example.UserStatus"
      },
      "fieldTypes": {
        "preferences": "com.example.UserPreferences"
      },
      "fieldDefaults": {
        "roles": "new HashSet<>()",
        "createdAt": "Instant.now()"
      },
      "readOnlyFields": ["id", "created_at"],
      "toStringIgnoreFields": ["password_hash"]
    }
  }
}
```

**Table Options:**
- `interfaces`: Implement additional interfaces
- `fieldNames`: Rename fields from database column names
- `additionalFields`: Add fields not in database, it could be:
    - simple types: **int**, **boolean**, etc
    - object types: **Integer**, **String**, etc
    - collections: **Map**, **List** and **Set**
    - complex types - any compile-time reachable from this class, with full package name, e.g: **other.package.BeanClass**
- `enumFields`: Map columns to enum types
- `fieldTypes`: Override field types
- `fieldDefaults`: Set default values for fields
- `readOnlyFields`: Fields without setters (table-specific)
- `toStringIgnoreFields`: Exclude fields from toString()

## Usage

### Command Line

```bash
# Use configuration file
java -jar db2dto-1.20.0.jar -c myconfig.conf

# Override connection settings
java -jar db2dto-1.20.0.jar -c myconfig.conf \
  --url jdbc:postgresql://localhost/mydb \
  --user myuser \
  --password mypass \
  --schema public
```

**Command Line Options:**
- `-c, --config <file>`: Configuration file (default: db2dto.conf)
- `-d, --url <url>`: Database connection string
- `-u, --user <user>`: Database username
- `-p, --password <pass>`: Database password
- `-s, --schema <schema>`: Database schema
- `-h`: Show help

### Gradle Integration

```gradle
task generateDto(type: JavaExec) {
  classpath = sourceSets.main.runtimeClasspath
  main = 'dev.walgo.db2dto.Main'
  args '--config', 'db2dto.conf'
}

// Or programmatically
task generateDto() {
  doLast {
    File configFile = new File('db2dto.conf')
    com.google.gson.Gson gson = new com.google.gson.Gson()
    def config = gson.fromJson(configFile.text, dev.walgo.db2dto.config.Config.class)
    
    // Override settings
    config.dbURL = 'jdbc:postgresql://localhost/mydb'
    config.dbUser = project.findProperty('dbUser') ?: 'defaultUser'
    config.dbPassword = project.findProperty('dbPassword') ?: 'defaultPass'
    
    def processor = new dev.walgo.db2dto.Processor()
    processor.setConfig(config)
    processor.execute()
  }
}
```

### Maven Integration

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>3.1.0</version>
  <executions>
    <execution>
      <id>generate-dto</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>java</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <mainClass>dev.walgo.db2dto.Main</mainClass>
    <arguments>
      <argument>--config</argument>
      <argument>db2dto.conf</argument>
    </arguments>
  </configuration>
</plugin>
```

## Generated Code Features

### Change Tracking

Every generated DTO implements the `IData` interface with change tracking:

```java
public interface IData {
  boolean hasChangedField(String fieldName);
  boolean isChanged();
  void resetChangedField(String fieldName);
  void resetChanged();
  Set<String> getFieldNames();
}
```

### Smart Setters

Generated setters only mark fields as changed when values actually differ:

```java
public UserData setName(String newValue) {
  if (!Objects.equals(newValue, this.name)) {
    this.name = newValue;
    changedFields.add("name");
  }
  return this;
}

// Non-null setter variant
public UserData setNameNotNull(String newValue) {
  if (!Objects.equals(newValue, this.name) && newValue != null) {
    this.name = newValue;
    changedFields.add("name");
  }
  return this;
}
```

### Field Change Methods

Each field gets individual change tracking methods:

```java
// Mark field as changed
public UserData setNameChanged() {
  changedFields.add("name");
  return this;
}

// Check if field changed
public boolean isNameChanged() {
  return changedFields.contains("name");
}
```

## Templates

DB2DTO uses Pebble templates for code generation. Built-in templates:

- `class.tpl`: Standard DTO classes
- `interface.tpl`: Interface generation
- `panache/class.tpl`: Quarkus Panache entities

### Custom Templates

Create custom templates in your template directory:

```pebble
package {{ packageName }};

{% for import in imports %}
import {{ import }};
{% endfor %}

public class {{ className }} {{ extendsClause }}{{ implementsClause }} {
  {% for field in fields %}
  private {{ field.type }} {{ field.name }}{{ field.defaultValue }};
  {% endfor %}
  
  // Your custom template content
}
```

## Plugin System

Extend DB2DTO with custom plugins for database-specific handling:

```java
public class CustomPlugin implements IPlugin {
  @Override
  public String convertType(String sqlType, int length, int scale) {
    // Custom type conversion logic
    return "CustomType";
  }
}
```

Register plugins in configuration:

```json
{
  "pluginPackages": ["com.example.plugins"]
}
```

## Database Support

DB2DTO works with any JDBC-compliant database. Tested with:

- ✅ PostgreSQL
- ✅ HSQLDB
- ✅ MySQL/MariaDB
- ✅ Oracle
- ✅ SQL Server
- ✅ H2

## Development

### Building from Source

```bash
git clone https://github.com/wwalery/db2dto.git
cd db2dto
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Creating Distribution

```bash
./gradlew shadowJar
# Creates db2dto-{version}.jar in build/libs/
```

## Examples

See the `examples/` directory for:
- Sample configuration files
- Database schemas
- Generated code examples

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Changelog

See [ChangeLog.md](ChangeLog.md) for version history and changes.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) file for details.

## Author

**Walery Wysotsky** - [dev@wysotsky.info](mailto:dev@wysotsky.info)

---

⭐ Star this repo if you find it useful!