/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.springframework.jmx.metadata.support;

/**
 * @author Rob Harrop
 */
public class ManagedAttribute extends AbstractJmxAttribute {

    public static final ManagedAttribute EMPTY = new ManagedAttribute();
    
    private Object defaultValue;
    
    public ManagedAttribute() {
        description = "";
    }
    
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
}
