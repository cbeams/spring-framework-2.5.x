/*
 * Created on Oct 19, 2004
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