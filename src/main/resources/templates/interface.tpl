package {{ config.packageName("") }};

import java.util.Set;

public interface {{ config.baseInterfaceName }} {

  boolean hasChangedField(final String fieldName);

  boolean isChanged();

  void resetChangedField(final String fieldName);

  void resetChanged();

  Set<String> getFieldNames();

}
