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

package org.springframework.remoting.jaxrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Stub;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.remoting.RemoteAccessException;

/**
 * @author Juergen Hoeller
 * @since 18.12.2003
 */
public class JaxRpcTestSuite extends TestCase {

	public void testLocalJaxRpcServiceFactoryBeanWithWsdlAndNamespace() throws Exception {
		LocalJaxRpcServiceFactoryBean factory = new LocalJaxRpcServiceFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setNamespaceUri("myNamespace1");
		factory.setServiceName("myService1");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertEquals(MockServiceFactory.service1, factory.getObject());
	}

	public void testLocalJaxRpcServiceFactoryBeanWithoutWsdlAndNamespace() throws Exception {
		LocalJaxRpcServiceFactoryBean factory = new LocalJaxRpcServiceFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setServiceName("myService2");
		factory.afterPropertiesSet();
		assertEquals(MockServiceFactory.service2, factory.getObject());
	}

	public void testJaxRpcPortProxyFactoryBean() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setNamespaceUri("myNamespace1");
		factory.setServiceName("myService1");
		factory.setPortName("myPort1");
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

	public void testJaxRpcPortProxyFactoryBeanWithRemoteException() throws Exception {
		JaxRpcPortProxyFactoryBean factory = new JaxRpcPortProxyFactoryBean();
		factory.setServiceFactoryClass(MockServiceFactory.class);
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setNamespaceUri("myNamespace1");
		factory.setServiceName("myService1");
		factory.setPortName("myPort1");
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
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setNamespaceUri("myNamespace1");
		factory.setServiceName("myService1");
		factory.setPortName("myPort1");
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
		factory.setWsdlDocumentUrl(new URL("http://myUrl1"));
		factory.setNamespaceUri("myNamespace1");
		factory.setServiceName("myService1");
		factory.setPortName("myPort1");
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

		private static MockControl service1Control;
		private static Service service1;
		private static MockControl service2Control;
		private static Service service2;

		public MockServiceFactory() throws ServiceException {
			service1Control = MockControl.createControl(Service.class);
			service1 = (Service) service1Control.getMock();
			service2Control = MockControl.createControl(Service.class);
			service2 = (Service) service2Control.getMock();
			service1.getPort(new QName("myNamespace1", "myPort1"), IRemoteBean.class);
			service1Control.setReturnValue(new RemoteBean());
			service1Control.replay();
		}

		public Service createService(URL url, QName qName) throws ServiceException {
			try {
				if (!(new URL("http://myUrl1")).equals(url) || !"myNamespace1".equals(qName.getNamespaceURI()) ||
						!"myService1".equals(qName.getLocalPart())) {
					throw new ServiceException("not supported");
				}
			}
			catch (MalformedURLException ex) {
			}
			return service1;
		}

		public Service createService(QName qName) throws ServiceException {
			if (!"".equals(qName.getNamespaceURI()) || !"myService2".equals(qName.getLocalPart())) {
				throw new ServiceException("not supported");
			}
			return service2;
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
