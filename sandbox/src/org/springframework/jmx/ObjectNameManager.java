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
package org.springframework.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author robh
 *  
 */
public class ObjectNameManager {

    private static Map objectNameCache = new HashMap();

    public static ObjectName getInstance(String objectName)
            throws MalformedObjectNameException {
        ObjectName name = (ObjectName) objectNameCache.get(objectName);

        if (name == null) {
            name = new ObjectName(objectName);
            synchronized (objectNameCache) {
                objectNameCache.put(objectName, name);
            }
        }

        return name;
    }
}