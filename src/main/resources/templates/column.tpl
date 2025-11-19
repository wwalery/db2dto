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
    if (this.{{ column.javaFieldName }} != value) {
      this.{{ column.javaFieldName }} = value;
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
{% endif %}

  public boolean get{{ column.javaPropertyName }}Bool() {
    return this.{{ column.javaFieldName }} == 1;
  }
#}
  

{% if (config.isEnum(table.name, column.name)) %}

{%  if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final {{ config.getEnum(table.name, column.name) }} newValue) {
    if (newValue == null) {
      if (this.{{ column.javaFieldName }} != null) {
        this.{{ column.javaFieldName }} = null;
        changedFields.add({{ column.name | upper }});
      }
    } else if (!Objects.equals(newValue.name(), this.{{ column.javaFieldName }})) {
      this.{{ column.javaFieldName }} = newValue.name();
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
{%  endif %}

  public {{ config.getEnum(table.name, column.name) }} get{{ column.javaPropertyName }}Enum() {
    return this.{{ column.javaFieldName }} == null ? null : {{ config.getEnum(table.name, column.name) }}.valueOf(this.{{ column.javaFieldName }});
  }
{% endif %}
  
{%  if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}(final {{ columnType | raw }} newValue) {
{%    if (column.isSimpleType) %}
    if (newValue != this.{{ column.javaFieldName }}) {
{%    else %}
    if (!Objects.equals(newValue, this.{{ column.javaFieldName }})) {
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
    if ((newValue != null) && (newValue != this.{{ column.javaFieldName }})) {
{%      else %}
    if (!Objects.equals(newValue, this.{{ column.javaFieldName }}) && (newValue != null)) {
{%      endif %}
      this.{{ column.javaFieldName }} = newValue;
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }
  
  
  /**
  * Update field value via Optional - only when the previous value is not the same.
  *
  * if newValue is null - data is not set
  * if newValue is empty - data is set to null (if previous value is not null)
  * else - set data from newValue (if previous value is not equal to newValue)
  */
  public {{ table.javaName }} set{{ column.javaPropertyName }}Optional(final Optional<{{ column.javaType | raw }}> newValue) {
    if (newValue == null) {
      return this;
    }
    if (newValue.isEmpty()) {
{%      if (column.isSimpleType) %}
      throw new RuntimeException("Null value doesn't allowed for field: {{ table.name }}.{{ column.name }}");
{%      else %}
      if (this.{{ column.javaFieldName }} != null) {
        this.{{ column.javaFieldName }} = null;
        changedFields.add({{ column.name | upper }});
      }
{%    endif %}
    } else if (!Objects.equals(newValue.get(), this.{{ column.javaFieldName }})) {
      this.{{ column.javaFieldName }} = newValue.get();
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }

  /**
  * Update field value via Optional.
  *
  * if newValue is null - data is not set
  * if newValue is empty - data is set to null
  * else - set data from newValue
  */
  public {{ table.javaName }} set{{ column.javaPropertyName }}OptionalForced(final Optional<{{ column.javaType | raw }}> newValue) {
    if (newValue == null) {
      return this;
    }
    if (newValue.isEmpty()) {
{%      if (column.isSimpleType) %}
      throw new RuntimeException("Null value doesn't allowed for field: {{ table.name }}.{{ column.name }}");
{%      else %}
      this.{{ column.javaFieldName }} = null;
{%    endif %}
    } else {
      this.{{ column.javaFieldName }} = newValue.get();
    }
    changedFields.add({{ column.name | upper }});
    return this;
  }
  
{%    endif %}
{%  endif %}

  public {{ columnType | raw }} get{{ column.javaPropertyName }}() {
    return this.{{ column.javaFieldName }};
  }

{%  if (column.isNullable and not column.isSimpleType and column.hasDefaultValue) %}
  public {{ column.javaType | raw }} get{{ column.javaPropertyName }}NonNull() {
    if (this.{{ column.javaFieldName }} == null) {
      this.{{ column.javaFieldName }} = {{ column.defaultValue | raw }};
    }
    return this.{{ column.javaFieldName }};
  }
{%  endif %}


{% if (column.javaType == 'java.sql.Timestamp') %}
{%   if (not config.isReadOnlyField(table.name, column.name)) %}
  public {{ table.javaName }} set{{ column.javaPropertyName }}AsDateTime(final LocalDateTime newValue) {
    if (!Objects.equals(newValue, this.{{ column.javaFieldName }}) && (newValue != null)) {
      this.{{ column.javaFieldName }} = java.sql.Timestamp.valueOf(newValue);
      changedFields.add({{ column.name | upper }});
    }
    return this;
  }

  public {{ table.javaName }} set{{ column.javaPropertyName }}AsInstant(final Instant newValue) {
    if (!Objects.equals(newValue, this.{{ column.javaFieldName }}) && (newValue != null)) {
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
    if (!Objects.equals(newValue, this.{{ column.javaFieldName }}) && (newValue != null)) {
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

