/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.values;

import java.beans.PropertyEditor;

/**
 * @author Keith Donald
 */
public interface MutableAspectAccessStrategy extends AspectAccessStrategy {
    public void registerCustomEditor(Class aspectType,
            PropertyEditor propertyEditor);

    public void registerCustomEditor(Class aspectType, String aspect,
            PropertyEditor propertyEditor);

    public PropertyEditor findCustomEditor(Class aspectType, String aspect);

    public boolean isValueUpdating();
    
    public void setValue(String aspect, Object value);

    public MutableAspectAccessStrategy newNestedAccessor(
            ValueModel parentValueHolder);

    public ValueModel getDomainObjectHolder();

    public void addValueListener(ValueListener listener, String aspect);

    public void removeValueListener(ValueListener listener, String aspect);

}