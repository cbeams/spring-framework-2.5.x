/*
 * Created on Oct 19, 2004
 */
package org.springframework.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

/**
 * @author robh
 *  
 */
public class DynamicMBeanTest implements DynamicMBean {

	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {

		if ("name".equals(attribute)) {
			return "Rob Harrop";
		} else {
			return null;
		}
	}

	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {

	}

	public AttributeList getAttributes(String[] arg0) {

		return null;
	}

	public AttributeList setAttributes(AttributeList arg0) {

		return null;
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {

		return null;
	}

	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo attr = new MBeanAttributeInfo("name",
				"java.lang.String",
				"",
				true, false, false);
		
		MBeanInfo inf = new MBeanInfo(DynamicMBeanTest.class.getName(), 
				"", 
				new MBeanAttributeInfo[]{attr},
				new MBeanConstructorInfo[0],
				new MBeanOperationInfo[0],
				new MBeanNotificationInfo[0]);
		
		return inf;
	}

}