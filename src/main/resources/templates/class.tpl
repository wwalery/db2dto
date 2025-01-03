package {{ config.packageName(table.name) }};

import java.util.*;
import java.time.*;
{% if (config.packageName("") != config.packageName(table.name)) %}
import {{ config.packageName("") }}.{{ config.baseInterfaceName }};
{% endif %}
{% for interface in config.getInterfaces(table.name) %}
import {{ interface }};
{% endfor %}
{% for enum in config.getEnums(table.name).values() %}
import {{ enum }};
{% endfor %}

public class {{ table.javaName }} implements {{ config.baseInterfaceName }}{% for interface in config.getInterfaces(table.name) %}, {{ interface }}{% endfor %} {

  public static final String TABLE = "{{ table.name }}";

{% for column in table.columns %}
  public static final String {{ column.name | upper }} = "{{ column.name }}";
{% endfor %}
{% for column in config.fields(table.name) %}
  public static final String {{ column.name | upper }} = "{{ column.name }}";
{% endfor %}

  private static final Map<String, String> FIELDS = Map.ofEntries(
    {% for column in table.columns %}Map.entry({{ column.name | upper }},"{{ column.sqlTypeName }}"){% if not loop.last %}, 
    {% endif %}{% endfor %});

  private final Set<String> changedFields = new HashSet<>();

{% for column in table.columns %}
  private {% if column.isNullable %}{{ column.javaType | raw }}{% else %}{{ column.simpleJavaType | raw }}{% endif %} {{ column.javaFieldName }}{% if column.hasDefaultValue and config.isUseDefaults(table.name) %} = {{ column.defaultValue | raw }}{% endif %};
{% endfor %}

{% for column in config.fields(table.name) %}
  private {{ column.javaType | raw }} {{ column.javaFieldName }};
{% endfor %}

  public {{ table.javaName }}() {
    // empty constructor
  }

  public {{ table.javaName }}({{ table.javaName }} source) {
    if (source == null) {
      return;
    }
{% for column in table.columns %}
    this.{{ column.javaFieldName }} = source.get{{ column.javaPropertyName }}();
{% endfor %}
{% for column in config.fields(table.name) %}
    this.{{ column.javaFieldName }} = source.get{{ column.javaPropertyName }}();
{% endfor %}
  }



  public {{ table.javaName }}(
{% for column in table.columns %}
      {{ column.javaType | raw }} {{ column.javaFieldName }}{% if not loop.last %},
{% endif %}{% endfor %}
  ) {
{% for column in table.columns %}
    this.{{ column.javaFieldName }} = {{ column.javaFieldName }};
{% endfor %}
  }

{% if not (config.fields(table.name) is empty) %}
  public {{ table.javaName }}(
{% for column in table.columns %}
      {{ column.javaType | raw }} {{ column.javaFieldName }},
{% endfor %}
{% for column in config.fields(table.name) %}
      {{ column.javaType | raw }} {{ column.javaFieldName }}{% if not loop.last %},
{% endif %}{% endfor %}
  ) {
{% for column in table.columns %}
    this.{{ column.javaFieldName }} = {{ column.javaFieldName }};
{% endfor %}
{% for column in config.fields(table.name) %}
    this.{{ column.javaFieldName }} = {{ column.javaFieldName }};
{% endfor %}
  }
{% endif %}


  public boolean isFieldChanged(final String fieldName) {
    return changedFields.contains(fieldName);
  }

  public boolean isChanged() {
    return !changedFields.isEmpty();
  }

  public void resetChangedField(final String fieldName) {
    changedFields.remove(fieldName);
  }

  public void resetChanged() {
    changedFields.clear();
  }


  public Set<String> getFieldNames() {
    return FIELDS.keySet();
  }

  public String getSQLType(final String fieldName) {
    return FIELDS.get(fieldName);
  }

  public Map<String, String> getSQLFields() {
    return FIELDS;
  }

  public Map<String, Object> getValues(final boolean onlyChanged) {
    Map<String, Object> result = new HashMap<>();
{% for column in table.columns %}
    if (!onlyChanged || changedFields.contains({{ column.name | upper }})) {
      result.put({{ column.name | upper }}, get{{ column.javaPropertyName }}());
    }
{% endfor %}
{% for column in config.fields(table.name) %}
    if (!onlyChanged) {
      result.put({{ column.name | upper }}, get{{ column.javaPropertyName }}());
    }
{% endfor %}
    return result;
  }



{% for column in table.columns %}
{% include "./column.tpl" %}
{% endfor %}

{% for column in config.fields(table.name) %}
{% include "./column.tpl" %}
{% endfor %}

  public String toString() {
    return {% for column in config.getToStringFields(table) %}"{{ column.javaFieldName }}=" + {{ column.javaFieldName }}{% if not loop.last %} + ", " + {% endif %}{% endfor %};
  }


}
