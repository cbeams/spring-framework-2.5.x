/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jmx.export;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jmx.AbstractMBeanServerTests;
import org.springframework.jmx.IJmxTestBean;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.jmx.support.ObjectNameManager;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.modelmbean.ModelMBeanInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration tests for the MBeanExporter class.
 * 
 * @author Rob Harrop
 * @author Rick Evans
 */
public class MBeanExporterTests extends AbstractMBeanServerTests {

	private static final String OBJECT_NAME = "spring:test=jmxMBeanAdaptor";


	public void testRegisterNonNotificationListenerType() throws Exception {
		Map listeners = new HashMap();
		// put a non-NotificationListener instance in as a value...
		listeners.put("*", this);
		MBeanExporter exporter = new MBeanExporter();
		try {
			exporter.setNotificationListenerMappings(listeners);
			fail("Must have thrown an IllegalArgumentException when registering a non-NotificationListener instance as a NotificationListener.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testRegisterNullNotificationListenerType() throws Exception {
		Map listeners = new HashMap();
		// put null in as a value...
		listeners.put("*", null);
		MBeanExporter exporter = new MBeanExporter();
		try {
			exporter.setNotificationListenerMappings(listeners);
			fail("Must have thrown an IllegalArgumentException when registering a null instance as a NotificationListener.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testRegisterNotificationListenerForNonExistentMBean() throws Exception {
		Map listeners = new HashMap();
		NotificationListener dummyListener = new NotificationListener() {
			public void handleNotification(Notification notification, Object handback) {
				throw new UnsupportedOperationException();
			}
		};
		// the MBean with the supplied object name does not exist...
		listeners.put("spring:type=Test", dummyListener);
		MBeanExporter exporter = new MBeanExporter();
		exporter.setBeans(getBeanMap());
		exporter.setServer(server);
		exporter.setNotificationListenerMappings(listeners);
		try {
			exporter.afterPropertiesSet();
			fail("Must have thrown an MBeanExportException when registering a NotificationListener on a non-existent MBean.");
		}
		catch (MBeanExportException expected) {
			assertTrue(expected.contains(InstanceNotFoundException.class));
		}
	}

	public void testWithSuppliedMBeanServer() throws Exception {
		MBeanExporter adaptor = new MBeanExporter();
		adaptor.setBeans(getBeanMap());
		adaptor.setServer(server);
		adaptor.afterPropertiesSet();
		assertIsRegistered("The bean was not registered with the MBeanServer", ObjectNameManager.getInstance(OBJECT_NAME));
	}

	public void testWithLocatedMBeanServer() throws Exception {
		MBeanExporter adaptor = new MBeanExporter();
		adaptor.setBeans(getBeanMap());
		adaptor.afterPropertiesSet();
		assertIsRegistered("The bean was not registered with the MBeanServer", ObjectNameManager.getInstance(OBJECT_NAME));
		server.unregisterMBean(new ObjectName(OBJECT_NAME));
	}

	public void testUserCreatedMBeanRegWithDynamicMBean() throws Exception {
		Map map = new HashMap();
		map.put("spring:name=dynBean", new TestDynamicMBean());

		InvokeDetectAssembler asm = new InvokeDetectAssembler();

		MBeanExporter adaptor = new MBeanExporter();
		adaptor.setServer(server);
		adaptor.setBeans(map);
		adaptor.setAssembler(asm);
		adaptor.afterPropertiesSet();

		Object name = server.getAttribute(ObjectNameManager.getInstance("spring:name=dynBean"), "name");
		assertEquals("The name attribute is incorrect", "Rob Harrop", name);
		assertFalse("Assembler should not have been invoked", asm.invoked);
	}

	public void testAutodetectMBeans() throws Exception {
		XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("autodetectMBeans.xml", getClass()));
		try {
			bf.getBean("exporter");
			MBeanServer server = (MBeanServer) bf.getBean("server");
			ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance("spring:mbean=true"));
			assertNotNull(instance);
		}
		finally {
			bf.destroySingletons();
		}
	}

	public void testAutodetectWithExclude() throws Exception {
		XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("autodetectMBeans.xml", getClass()));
		try {
			bf.getBean("exporter");
			MBeanServer server = (MBeanServer) bf.getBean("server");
			ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance("spring:mbean=true"));
			assertNotNull(instance);

			try {
				server.getObjectInstance(ObjectNameManager.getInstance("spring:mbean=false"));
				fail("MBean with name spring:mbean=false should have been excluded");
			}
			catch (InstanceNotFoundException ex) {
				// success
			}
		}
		finally {
			bf.destroySingletons();
		}
	}

	public void testAutodetectLazyMBeans() throws Exception {
		XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("autodetectLazyMBeans.xml", getClass()));
		try {
			bf.getBean("exporter");
			MBeanServer server = (MBeanServer) bf.getBean("server");
			ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance("spring:mbean=true"));
			assertNotNull(instance);
		}
		finally {
			bf.destroySingletons();
		}
	}

	public void testWithMBeanExporterListeners() throws Exception {
		MockMBeanExporterListener listener1 = new MockMBeanExporterListener();
		MockMBeanExporterListener listener2 = new MockMBeanExporterListener();

		MBeanExporter adaptor = new MBeanExporter();
		adaptor.setBeans(getBeanMap());
		adaptor.setServer(server);
		adaptor.setListeners(new MBeanExporterListener[]{listener1, listener2});
		adaptor.afterPropertiesSet();
		adaptor.destroy();
		
		assertListener(listener1);
		assertListener(listener2);
	}

	public void testExportJdkProxy() throws Exception {
		JmxTestBean bean = new JmxTestBean();
		bean.setName("Rob Harrop");

		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(bean);
		factory.addAdvice(new NopInterceptor());
		factory.setInterfaces(new Class[]{IJmxTestBean.class});

		IJmxTestBean proxy = (IJmxTestBean) factory.getProxy();
		String name = "bean:proxy=true";

		Map beans = new HashMap();
		beans.put(name, proxy);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.registerBeans();

		ObjectName oname = ObjectName.getInstance(name);
		Object nameValue = server.getAttribute(oname, "Name");
		assertEquals("Rob Harrop", nameValue);
	}

	public void testSelfNaming() throws Exception {
		ObjectName objectName = ObjectNameManager.getInstance(OBJECT_NAME);
		SelfNamingTestBean testBean = new SelfNamingTestBean();
		testBean.setObjectName(objectName);

		Map beans = new HashMap();
		beans.put("foo", testBean);


		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);

		exporter.afterPropertiesSet();

		ObjectInstance instance = server.getObjectInstance(objectName);
		assertNotNull(instance);
	}

	public void testRegisterIgnoreExisting() throws Exception {
		ObjectName objectName = ObjectNameManager.getInstance(OBJECT_NAME);

		Person preRegistered = new Person();
		preRegistered.setName("Rob Harrop");

		server.registerMBean(preRegistered, objectName);

		Person springRegistered = new Person();
		springRegistered.setName("Sally Greenwood");

		Map beans = new HashMap();
		beans.put(objectName.toString(), springRegistered);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setRegistrationBehavior(MBeanExporter.REGISTRATION_IGNORE_EXISTING);

		exporter.afterPropertiesSet();

		ObjectInstance instance = server.getObjectInstance(objectName);
		assertNotNull(instance);

		// should still be the first bean with name Rob Harrop
		assertEquals("Rob Harrop", server.getAttribute(objectName, "Name"));
	}

	public void testRegisterReplaceExisting() throws Exception {
		ObjectName objectName = ObjectNameManager.getInstance(OBJECT_NAME);

		Person preRegistered = new Person();
		preRegistered.setName("Rob Harrop");

		server.registerMBean(preRegistered, objectName);

		Person springRegistered = new Person();
		springRegistered.setName("Sally Greenwood");

		Map beans = new HashMap();
		beans.put(objectName.toString(), springRegistered);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setRegistrationBehavior(MBeanExporter.REGISTRATION_REPLACE_EXISTING);

		exporter.afterPropertiesSet();

		ObjectInstance instance = server.getObjectInstance(objectName);
		assertNotNull(instance);

		// should still be the new bean with name Sally Greenwood
		assertEquals("Sally Greenwood", server.getAttribute(objectName, "Name"));
	}

	public void testWithExposeClassLoader() throws Exception {
		String name = "Rob Harrop";
		String otherName = "Juergen Hoeller";

		JmxTestBean bean = new JmxTestBean();
		bean.setName(name);
		ObjectName objectName = ObjectNameManager.getInstance("spring:type=Test");

		Map beans = new HashMap();
		beans.put(objectName.toString(), bean);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(getServer());
		exporter.setBeans(beans);
		exporter.setExposeManagedResourceClassLoader(true);
		exporter.afterPropertiesSet();

		assertIsRegistered("Bean instance not registered", objectName);

		Object result = server.invoke(objectName, "add",
				new Object[]{new Integer(2), new Integer(3)},
				new String[]{int.class.getName(), int.class.getName()});

		assertEquals("Incorrect result return from add", result, new Integer(5));
		assertEquals("Incorrect attribute value", name, server.getAttribute(objectName, "Name"));

		server.setAttribute(objectName, new Attribute("Name", otherName));
		assertEquals("Incorrect updated name.", otherName, bean.getName());
	}


	private void assertListener(MockMBeanExporterListener listener) throws MalformedObjectNameException {
		ObjectName desired = ObjectNameManager.getInstance(OBJECT_NAME);
		assertEquals("Incorrect number of registrations", 1, listener.getRegistered().size());
		assertEquals("Incorrect number of unregistrations", 1, listener.getUnregistered().size());
		assertEquals("Incorrect ObjectName in register", desired, listener.getRegistered().get(0));
		assertEquals("Incorrect ObjectName in unregister", desired, listener.getUnregistered().get(0));
	}

	private Map getBeanMap() {
		Map map = new HashMap();
		map.put(OBJECT_NAME, new JmxTestBean());
		return map;
	}


	private static class InvokeDetectAssembler implements MBeanInfoAssembler {

		private boolean invoked = false;

		public ModelMBeanInfo getMBeanInfo(Object managedResource, String beanKey) throws JMException {
			invoked = true;
			return null;
		}
	}

	private static class MockMBeanExporterListener implements MBeanExporterListener {

		private List registered = new ArrayList();

		private List unregistered = new ArrayList();

		public void mbeanRegistered(ObjectName objectName) {
			registered.add(objectName);
		}

		public void mbeanUnregistered(ObjectName objectName) {
			unregistered.add(objectName);
		}

		public List getRegistered() {
			return registered;
		}

		public List getUnregistered() {
			return unregistered;
		}
	}

	private static class SelfNamingTestBean implements SelfNaming {

		private ObjectName objectName;

		public void setObjectName(ObjectName objectName) {
			this.objectName = objectName;
		}

		public ObjectName getObjectName() throws MalformedObjectNameException {
			return this.objectName;
		}
	}

	public static interface PersonMBean {

		String getName();
	}

	public static class Person implements PersonMBean {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
