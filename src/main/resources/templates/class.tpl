package {{ config.packageName(table.name) }};

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
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

  private final static Set<String> FIELD_NAMES = Stream.of({% for column in table.columns %}"{{ column.name }}"{% if not loop.last %}, {% endif %}
{% endfor %}).collect(Collectors.toSet());

  private final Set<String> changedFields = new HashSet<>();

{% for column in table.columns %}
  private {{ column.javaType | raw }} {{ column.javaFieldName }};
{% endfor %}

{% for column in config.fields(table.name) %}
  private {{ column.javaType | raw }} {{ column.javaFieldName }};
{% endfor %}


  public boolean hasChangedField(final String fieldName) {
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
    return FIELD_NAMES;
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
