package org.springframework.rules.values;

import java.beans.PropertyEditor;

/**
 * @author Keith Donald
 */
public interface PropertyEditorProvider {
    public PropertyEditor getPropertyEditor(String domainProperty);
}