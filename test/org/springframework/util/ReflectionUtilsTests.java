package org.springframework.util;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;

/**
 * @author Rob Harrop
 */
public class ReflectionUtilsTests extends TestCase {

	public void testInvokeMethod() throws Exception {
		String rob = "Rob Harrop";
		String juergen = "Juergen Hoeller";

		TestBean bean = new TestBean();
		bean.setName(rob);

		Method getName = TestBean.class.getMethod("getName", null);
		Method setName = TestBean.class.getMethod("setName", new Class[]{String.class});

		Object name = ReflectionUtils.invokeMethod(getName, bean);
		assertEquals("Incorrect name returned", rob, name);

		ReflectionUtils.invokeMethod(setName, bean, new Object[]{juergen});
		assertEquals("Incorrect name set", juergen, bean.getName());
	}

}
