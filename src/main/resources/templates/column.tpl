// {{ column.name }}

{% if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}Changed() {
    changedFields.add("{{ column.name }}");
    return this;
  }

  public boolean is{{ column.javaPropertyName }}Changed() {
    return changedFields.contains("{{ column.name }}");
  }
{% endif %}  

{#
// GET/SET boolean
{% if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final boolean newValue) {
    byte value = newValue ?  (byte) 1 : (byte) 0);
    if ({{ column.javaFieldName }} != value) {
      {{ column.javaFieldName }} = value;
      changedFields.add("{{ column.name }}");
    }
    return this;
  }
{% endif %}  

  public boolean get{{ column.javaPropertyName }}Bool() {
    return {{ column.javaFieldName }} == 1;
  }
#}
  

{% if (config.isEnum(table.name, column.name)) %}

{%  if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final {{ config.getEnum(table.name, column.name) }} newValue) {
    if (!java.util.Objects.equals(newValue.name(), {{ column.javaFieldName }})) {
      this.{{ column.javaFieldName }} = newValue.name();
      changedFields.add("{{ column.name }}");
    }
    return this;
  }
{% endif %}  

  public {{ config.getEnum(table.name, column.name) }} get{{ column.javaPropertyName }}Enum() {
    return this.{{ column.javaFieldName }} == null ? null : {{ config.getEnum(table.name, column.name) }}.valueOf({{ column.javaFieldName }});
  }
{% endif %}  
  
{%  if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final {{ column.javaType | raw }} newValue) {
    if (!java.util.Objects.equals(newValue, {{ column.javaFieldName }})) {
      this.{{ column.javaFieldName }} = newValue;
      changedFields.add("{{ column.name }}");
    }
    return this;
  }

  public {{ table.javaName }} set{{ column.javaPropertyName }}NotNull(final {{ column.javaType | raw }} newValue) {
    if (!java.util.Objects.equals(newValue, {{ column.javaFieldName }}) && (newValue != null)) {
      this.{{ column.javaFieldName }} = newValue;
      changedFields.add("{{ column.name }}");
    }
    return this;
  }


{% endif %}  

  public {{ column.javaType | raw }} get{{ column.javaPropertyName }}() {
    return this.{{ column.javaFieldName }};
  }

