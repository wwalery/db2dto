package {{ config.packageName("") }};

import java.util.Set;

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

}

