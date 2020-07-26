package {{ config.packageName("") }};

import java.util.Set;
import java.util.Map;

public interface {{ config.baseInterfaceName }} {

  // check that file changed
  boolean hasChangedField(final String fieldName);
  
  // Any fields changed
  boolean isChanged();

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
   * Gets Map between field name and SQL field type.
   */
  public Map<String, String> getSQLFields();


}

