/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.remoting.jaxrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Stub;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.ArgumentsMatcher;

import org.springframework.remoting.RemoteAccessException;

/**
 * @author Juergen Hoeller
 * @since 18.12.2003
 */
public class JaxRpcSupportTests extends TestCase {

	public void testLocalJaxRpcServiceFactoryBeanWithWsdlAndNamespace() throws Exception {
		LocalJaxRpcServiceFactoryBean factory = new LocalJaxRpcServiceFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setServiceName("myService2");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertEquals(MockServiceFactory.service2, factory.getObject());
	}

	public void testLocalJaxRpcServiceFactoryBeanWithoutWsdlAndNamespace() throws Exception {
		LocalJaxRpcServiceFactoryBean factory = new LocalJaxRpcServiceFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.afterPropertiesSet();
		assertEquals(MockServiceFactory.service1, factory.getObject());
	}

	public void testJaxRpcPortProxyFactoryBean() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setServiceInterface(IRemoteBean.class);
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getPortStub() instanceof Stub);

		assertTrue(factory.getObject() instanceof IRemoteBean);
		IRemoteBean proxy = (IRemoteBean) factory.getObject();
		proxy.setName("myName");
		assertEquals("myName", RemoteBean.singleton.name);
		MockServiceFactory.service1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithProperties() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setUsername("user");
		factory.setPassword("pw");
		factory.setEndpointAddress("ea");
		factory.setMaintainSession(true);
		factory.setServiceInterface(IRemoteBean.class);
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());

		assertTrue(factory.getPortStub() instanceof Stub);
		Stub stub = (Stub) factory.getPortStub();
		assertEquals("user", stub._getProperty(Stub.USERNAME_PROPERTY));
		assertEquals("pw", stub._getProperty(Stub.PASSWORD_PROPERTY));
		assertEquals("ea", stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY));
		assertTrue(((Boolean) stub._getProperty(Stub.SESSION_MAINTAIN_PROPERTY)).booleanValue());

		assertTrue(factory.getObject() instanceof IRemoteBean);
		IRemoteBean proxy = (IRemoteBean) factory.getObject();
		proxy.setName("myName");
		assertEquals("myName", RemoteBean.singleton.name);
		MockServiceFactory.service1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithDynamicCalls() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(CallMockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setServiceInterface(IBusinessBean.class);
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());

		assertTrue(factory.getObject() instanceof IBusinessBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		proxy.setName("myName");
		MockServiceFactory.service1Control.verify();
		CallMockServiceFactory.call1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithDynamicCallsAndProperties() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(CallWithPropertiesMockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setUsername("user");
		factory.setPassword("pw");
		factory.setEndpointAddress("ea");
		factory.setMaintainSession(true);
		factory.setServiceInterface(IBusinessBean.class);
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertNull(factory.getPortStub());

		assertTrue(factory.getObject() instanceof IBusinessBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		proxy.setName("myName");
		MockServiceFactory.service1Control.verify();
		CallMockServiceFactory.call1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithRemoteException() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setServiceInterface(IRemoteBean.class);
		factory.afterPropertiesSet();

		assertTrue(factory.getPortStub() instanceof Stub);
		Stub stub = (Stub) factory.getPortStub();
		assertNull(stub._getProperty(Stub.USERNAME_PROPERTY));
		assertNull(stub._getProperty(Stub.PASSWORD_PROPERTY));
		assertNull(stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY));
		assertNull(stub._getProperty(Stub.SESSION_MAINTAIN_PROPERTY));

		assertTrue(factory.getObject() instanceof IRemoteBean);
		IRemoteBean proxy = (IRemoteBean) factory.getObject();
		try {
			proxy.setName("exception");
			fail("Should have thrown RemoteException");
		}
		catch (RemoteException ex) {
			// expected
		}
		MockServiceFactory.service1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithPortInterface() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setPortInterface(IRemoteBean.class);
		factory.setServiceInterface(IBusinessBean.class);
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof IBusinessBean);
		assertFalse(factory.getObject() instanceof IRemoteBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		proxy.setName("myName");
		assertEquals("myName", RemoteBean.singleton.name);
		MockServiceFactory.service1Control.verify();
	}

	public void testJaxRpcPortProxyFactoryBeanWithPortInterfaceAndRemoteException() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setNamespaceUri("myNamespace");
		factory.setServiceName("myService1");
		factory.setPortName("myPort");
		factory.setPortInterface(IRemoteBean.class);
		factory.setServiceInterface(IBusinessBean.class);
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof IBusinessBean);
		assertFalse(factory.getObject() instanceof IRemoteBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		try {
			proxy.setName("exception");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
		MockServiceFactory.service1Control.verify();
	}


	public static class MockServiceFactory extends ServiceFactory {

		protected static MockControl service1Control;
		protected static Service service1;
		protected static MockControl service2Control;
		protected static Service service2;

		public MockServiceFactory() throws Exception {
			service1Control = MockControl.createControl(Service.class);
			service1 = (Service) service1Control.getMock();
			service2Control = MockControl.createControl(Service.class);
			service2 = (Service) service2Control.getMock();
			initMocks();
			service1Control.replay();
		}

		protected void initMocks() throws Exception {
			service1.getPort(new QName("myNamespace", "myPort"), IRemoteBean.class);
			service1Control.setReturnValue(new RemoteBean());
		}

		public Service createService(URL url, QName qName) throws ServiceException {
			try {
				if (!(new URL("http://myUrl1")).equals(url) || !"".equals(qName.getNamespaceURI()) ||
						!"myService2".equals(qName.getLocalPart())) {
					throw new ServiceException("not supported");
				}
			}
			catch (MalformedURLException ex) {
			}
			return service2;
		}

		public Service createService(QName qName) throws ServiceException {
			if (!"myNamespace".equals(qName.getNamespaceURI()) || !"myService1".equals(qName.getLocalPart())) {
				throw new ServiceException("not supported");
			}
			return service1;
		}
	}


	public static class CallMockServiceFactory extends MockServiceFactory {

		protected static MockControl call1Control;
		protected static Call call1;

		public CallMockServiceFactory() throws Exception {
			super();
		}

		protected void initMocks() throws Exception {
			call1Control = MockControl.createControl(Call.class);
			call1 = (Call) call1Control.getMock();
			service1.createCall(new QName("myNamespace", "myPort"), "setName");
			service1Control.setReturnValue(call1);
			initCall();
			call1Control.replay();
		}

		protected void initCall() throws Exception {
			call1.invoke(new Object[] {"myName"});
			call1Control.setMatcher(new ArgumentsMatcher() {
				public boolean matches(Object[] objects, Object[] objects1) {
					return Arrays.equals((Object[]) objects[0], (Object[]) objects1[0]);
				}
				public String toString(Object[] objects) {
					return null;
				}
			});
			call1Control.setReturnValue(null);
		}
	}


	public static class CallWithPropertiesMockServiceFactory extends CallMockServiceFactory {

		public CallWithPropertiesMockServiceFactory() throws Exception {
		}

		protected void initCall() throws Exception {
			call1.setProperty(Call.USERNAME_PROPERTY, "user");
			call1Control.setVoidCallable();
			call1.setProperty(Call.PASSWORD_PROPERTY, "pw");
			call1Control.setVoidCallable();
			call1.setTargetEndpointAddress("ea");
			call1Control.setVoidCallable();
			call1.setProperty(Call.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
			call1Control.setVoidCallable();
			super.initCall();
		}
	}


	public static interface IBusinessBean {

		public void setName(String name);

	}


	public static interface IRemoteBean extends Remote {

		public void setName(String name) throws RemoteException;

	}


	public static class RemoteBean implements IRemoteBean, Stub {

		private static RemoteBean singleton;
		private static String name;
		private static Map properties;

		public RemoteBean() {
			singleton = this;
			properties = new HashMap();
		}

		public void setName(String nam) throws RemoteException {
			if ("exception".equals(nam)) {
				throw new RemoteException();
			}
			name = nam;
		}

		public void _setProperty(String key, Object o) {
			properties.put(key, o);
		}

		public Object _getProperty(String key) {
			return properties.get(key);
		}

		public Iterator _getPropertyNames() {
			return properties.keySet().iterator();
		}
	}

}
