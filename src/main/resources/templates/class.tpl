package {{ config.packageName(table.name) }};

import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
{% if (config.packageName("") != config.packageName(table.name)) %}
import {{ config.packageName("") }};
{% endif %}

public class {{ table.javaName }} implements {{ config.baseInterfaceName }}{% for interface in config.getInterfaces(table.name) %}, {{ interface }}{% endfor %} {

  private final static Set<String> FIELD_NAMES = Stream.of({% for column in table.columns %}"{{ column.name }}"{% if not loop.last %}, {% endif %}
{% endfor %}).collect(Collectors.toSet());

  private final Set<String> changedFields = new HashSet();

{% for column in table.columns %}
  private {{ column.javaType }} {{ column.javaFieldName }};
{% endfor %}

{% for column in config.fields(table.name) %}
  private {{ column.javaType }} {{ column.javaFieldName }};
{% endfor %}


  public boolean hasChangedField(final String fieldName) {
    return changedFields.contains(fieldName);
  }

  public boolean isChanged() {
    return !changedFields.isEmpty();
  }

  public void resetChangedField(final String fieldName) {
    return changedFields.remove(fieldName);
  }

  public void resetChanged() {
    return changedFields.clear();
  }


  public Set<String> getFieldNames() {
    return FIELD_NAMES;
  }

{% for column in table.columns %}
{% include "templates/column.tpl" %}
{% endfor %}

{% for column in config.fields(table.name) %}
{% include "templates/column.tpl" %}
{% endfor %}

  public String toString() {
    return {% for column in config.getToStringFields(table) %}"{{ column.javaFieldName }}=" + {{ column.javaFieldName }}{% if not loop.last %} + ", " + {% endif %}{% endfor %};
  }


}
