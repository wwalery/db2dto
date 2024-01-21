// {{ column }}
{% set columnType = column.isSimpleType ? column.simpleJavaType : column.javaType  %}

{% if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}Changed() {
    changedFields.add({{ column.name | upper }});
    return this;
  }

  public {{ table.javaName }} reset{{ column.javaPropertyName }}Changed() {
    changedFields.remove({{ column.name | upper }});
    return this;
  }

  public boolean is{{ column.javaPropertyName }}Changed() {
    return changedFields.contains({{ column.name | upper }});
  }
{% endif %}

  public String get{{ column.javaPropertyName }}SQLType() {
    return "{{ column.sqlTypeName }}";
  }

{#
// GET/SET boolean
{% if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final boolean newValue) {
    byte value = newValue ?  (byte) 1 : (byte) 0);
    if ({{ column.javaFieldName }} != value) {
      {{ column.javaFieldName }} = value;
      changedFields.add({{ column.name | upper }});
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
    if (newValue == null) {
      if ({{ column.javaFieldName }} != null) {
        this.{{ column.javaFieldName }} = null;
        changedFields.add({{ column.name | upper }});
      }
    } else if (!Objects.equals(newValue.name(), {{ column.javaFieldName }})) {
      this.{{ column.javaFieldName }} = newValue.name();
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
{%  endif %}

  public {{ config.getEnum(table.name, column.name) }} get{{ column.javaPropertyName }}Enum() {
    return this.{{ column.javaFieldName }} == null ? null : {{ config.getEnum(table.name, column.name) }}.valueOf({{ column.javaFieldName }});
  }
{% endif %}
  
{%  if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final {{ columnType | raw }} newValue) {
{%    if (column.isSimpleType) %}
    if (newValue != {{ column.javaFieldName }}) {
{%    else %}
    if (!Objects.equals(newValue, {{ column.javaFieldName }})) {
{%    endif %}
      this.{{ column.javaFieldName }} = newValue;
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }

{%    if (not config.isSyntheticField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}Force(final {{ columnType | raw }} newValue) {
    this.{{ column.javaFieldName }} = newValue;
    changedFields.add({{ column.name | upper }});
    return this;
  }

  public {{ table.javaName }} set{{ column.javaPropertyName }}NotNull(final {{ column.javaType | raw }} newValue) {
{%      if (column.isSimpleType) %}
    if ((newValue != null) && (newValue != {{ column.javaFieldName }})) {
{%      else %}
    if (!Objects.equals(newValue, {{ column.javaFieldName }}) && (newValue != null)) {
{%      endif %}
      this.{{ column.javaFieldName }} = newValue;
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
{%    endif %}
{%  endif %}

  public {{ columnType | raw }} get{{ column.javaPropertyName }}() {
    return this.{{ column.javaFieldName }};
  }

{%  if (column.isNullable and not column.isSimpleType) %}
  public {{ column.javaType | raw }} get{{ column.javaPropertyName }}NonNull() {
    return this.{{ column.javaFieldName }} != null ? this.{{ column.javaFieldName }} : {{ column.defaultValue | raw }};
  }
{%  endif %}


{% if (column.javaType == 'java.sql.Timestamp') %}
{%   if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}AsDateTime(final LocalDateTime newValue) {
    if (!Objects.equals(newValue, {{ column.javaFieldName }}) && (newValue != null)) {
      this.{{ column.javaFieldName }} = java.sql.Timestamp.valueOf(newValue);
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }

  public {{ table.javaName }} set{{ column.javaPropertyName }}AsInstant(final Instant newValue) {
    if (!Objects.equals(newValue, {{ column.javaFieldName }}) && (newValue != null)) {
      this.{{ column.javaFieldName }} = java.sql.Timestamp.from(newValue);
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }

{%   endif %}

  public LocalDateTime get{{ column.javaPropertyName }}AsDateTime() {
    return this.{{ column.javaFieldName }} != null ? this.{{ column.javaFieldName }}.toLocalDateTime() : null;
  }

  public Instant get{{ column.javaPropertyName }}AsInstant() {
    return this.{{ column.javaFieldName }} != null ? this.{{ column.javaFieldName }}.toInstant() : null;
  }

{% endif %}


{% if (column.javaType == 'java.sql.Date') %}
{%   if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}AsDate(final LocalDate newValue) {
    if (!Objects.equals(newValue, {{ column.javaFieldName }}) && (newValue != null)) {
      this.{{ column.javaFieldName }} = java.sql.Date.valueOf(newValue);
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
{%   endif %}

  public LocalDate get{{ column.javaPropertyName }}AsDate() {
    return this.{{ column.javaFieldName }} != null ? this.{{ column.javaFieldName }}.toLocalDate() : null;
  }

{% endif %}

