/*
 * Created on Oct 19, 2004
 */
package org.springframework.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author robh
 *  
 */
public class ObjectNameManager {

	public static ObjectName getInstance(String objectName)
			throws MalformedObjectNameException {
		return new ObjectName(objectName);
	}
}