package {{ config.packageName("") }};

import java.util.Set;
import java.util.Map;

public interface {{ config.baseInterfaceName }} {

  // check that file changed
  boolean isFieldChanged(final String fieldName);
  
  // Any fields changed
  boolean isChanged();

  // set changing flag for specific field
  void setChangedField(final String fieldName);

  // reset changing flag for specific field
  void resetChangedField(final String fieldName);
  
  // reset changing flag for all fields
  void resetChanged();
  
  // return all fields 
  Set<String> getFieldNames();

  /**
   * Gets SQL field type by its name.
   */
  String getSQLType(final String fieldName);

  /**
   * Gets Map between SQL field name and SQL field type.
   */
  Map<String, String> getSQLFields();

  /*
  * Gets map SQL field name and current field value.
  */
  Map<String, Object> getValues(final boolean onlyChanged);


}

