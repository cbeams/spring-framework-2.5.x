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
package org.springframework.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;

/**
 * <code>ObjectNamingStrategy</code> that uses the key passed to <code>JmxMBeanAdapter</code>
 * when registering the bean for JMX exposure.
 * @author Rob Harrop
 */
public class KeyNamingStrategy implements ObjectNamingStrategy {

    /**
     * Returns the value of the <code>key</code> parameter parsed into
     * an instance of <code>ObjectName</code>.
     * @param managedResource the bean requiring a name.
     * @param key the key used to store the managed resource in a <code>Map</code> when passing to Spring.
     */
    public ObjectName getObjectName(Object managedResource, String key) {
        try {
            return ObjectNameManager.getInstance(key);
        } catch (MalformedObjectNameException ex) {
            throw new ObjectNamingException("The supplied key: " + key
                    + " is not a valid ObjectName.", ex);
        }
    }
}
