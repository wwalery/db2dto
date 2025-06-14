package {{ config.packageName(table.name) }};

import java.util.*;
import java.time.*;
{% for enum in config.getEnums(table.name).values() %}
import {{ enum }};
{% endfor %}
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "{{ table.realName }}")
public class {{ table.javaName }} extends PanacheEntityBase {

{% for column in table.columns %}
  {% if column.isPrimaryKey %}
  @Id
  @GeneratedValue
  {% endif %}
  public {% if column.isNullable %}{{ column.javaType | raw }}{% else %}{{ column.simpleJavaType | raw }}{% endif %} {{ column.javaFieldName }}{% if column.hasDefaultValue and config.isUseDefaults(table.name) %} = {{ column.defaultValue | raw }}{% endif %};
{% endfor %}

{% for column in config.fields(table.name) %}
  public {{ column.javaType | raw }} {{ column.javaFieldName }};
{% endfor %}


  public String toString() {
    return {% for column in config.getToStringFields(table) %}"{{ column.javaFieldName }}=" + {{ column.javaFieldName }}{% if not loop.last %} + ", " + {% endif %}{% endfor %};
  }


}
