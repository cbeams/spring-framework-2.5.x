/*
 * Created on Jul 8, 2004
 */
package org.springframework.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.ObjectName;

import org.springframework.util.StopWatch;

/**
 * @author robh
 *
 */
public abstract class AbstractJmxInvokerTests extends AbstractJmxTests {

	public AbstractJmxInvokerTests(String name) {
		super(name);
	}
	
	protected abstract String getObjectName();
	protected abstract MBeanInvoker getInvoker();
	
	public void testGetAttribute() throws Exception {
		Object value = server.getAttribute(ObjectNameManager.getInstance(getObjectName()),
				"name");
		assertNotNull("Result should not be null", value);
		assertEquals("The name should be TEST", value, "TEST");
	}

	public void testGetAttributes() throws Exception {
		String[] attrs = new String[]{"name", "age"};

		AttributeList list = server.getAttributes(ObjectName
				.getInstance(getObjectName()), attrs);

		assertNotNull("The attribute list should not be null", list);

		assertTrue("There should be two attributes registered for the bean: "
				+ getObjectName(), (list.size() == 2));
	}

	public void testSetAttribute() throws Exception {

		// get the underlying bean
		JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");

		String newVal = "Updated";

		server.setAttribute(ObjectNameManager.getInstance(getObjectName()), new Attribute(
				"name", newVal));

		assertEquals("The name property should have been updated", newVal, bean
				.getName());
	}

	public void testSetAttributes() throws Exception {

		// get the underlying bean
		JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");

		String newName = "updated";
		int newAge = 99;
		AttributeList al = new AttributeList();
		al.add(new Attribute("age", new Integer(newAge)));
		al.add(new Attribute("name", newName));

		server.setAttributes(ObjectNameManager.getInstance(getObjectName()), al);

		assertEquals("The name property should have been updated", bean
				.getName(), newName);
		assertEquals("The age property should have been updated",
				bean.getAge(), newAge);
	}

	public void testInvokeNoArgs() throws Exception {
		JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");
		Integer hashCode = (Integer) server.invoke(ObjectName
				.getInstance(getObjectName()), "hashCode", null, null);
		assertEquals("The hash codes should be equal", hashCode.intValue(), bean.hashCode());
	}

	public void testInvokeWithArgs() throws Exception {
		JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");
		Integer result = (Integer) server.invoke(ObjectName
				.getInstance(getObjectName()), "add", new Object[]{new Integer(2),
				new Integer(3)}, new String[]{int.class.getName(), int.class.getName()});
		assertEquals("The result of the addition should be 5", result.intValue(), 5);
	}
	
	public void testMultiInvoke() throws Exception {
		JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");
		
		ObjectName objectName = ObjectNameManager.getInstance(getObjectName());
		
		StopWatch sw = new StopWatch();
		
		sw.start("hashCode");
		for(int x = 0; x < 1000000; x++) {
			server.invoke(objectName, "hashCode", null, null);
		}
		
		sw.stop();
		System.err.println("testMultiInvoke() took:" + sw.getTotalTimeSeconds() + "ms");
		assertTrue("testMultiInvoke() took too long", (sw.getTotalTimeSeconds() < 20.0));
	}
	
	public void testInvalidAttributeInvoke() throws Exception {
		try {
			server.invoke(ObjectNameManager.getInstance(getObjectName()), "getAge", null, null);
			fail("Accessing an attribute using invoke is not allowed");
		} catch(MBeanException ex) {
			
		}
	}
	
	public void testMultiBeanRegistration() throws Exception{
		JmxTestBean bean1 = new JmxTestBean();
		JmxTestBean bean2 = new JmxTestBean();
		
		bean1.setName("Rob Harrop");
		bean2.setName("Harry Potter");
		
		Map beans = new HashMap();
		beans.put("spring:name=mb1", bean1);
		beans.put("spring:name=mb2", bean2);
		
		JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
		adaptor.setServer(server);
		adaptor.setBeans(beans);
		adaptor.registerBeans();
		
		// test the attribute values
		Object name1 = server.getAttribute(ObjectNameManager.getInstance("spring:name=mb1"), "name");
		assertEquals("Name is not correct", "Rob Harrop", name1);
		
		Object name2 = server.getAttribute(ObjectNameManager.getInstance("spring:name=mb2"), "name");
		assertEquals("Name is not correct", "Harry Potter", name2);
	}
}
