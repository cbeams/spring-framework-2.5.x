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

package org.springframework.remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.StubNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.rpc.Stub;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.remoting.caucho.BurlapProxyFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.rmi.RmiClientInterceptor;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

/**
 * @author Juergen Hoeller
 * @since 16.05.2003
 */
public class RemotingTestSuite extends TestCase {

	public void testRmiProxyFactoryBean() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean() {
			protected Remote createRmiProxy() {
				return new RemoteBean();
			}
		};
		factory.setServiceInterface(IRemoteBean.class);
		factory.setServiceUrl("rmi://localhost:1090/test");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof IRemoteBean);
		IRemoteBean proxy = (IRemoteBean) factory.getObject();
		proxy.setName("myName");
		assertEquals(RemoteBean.name, "myName");
	}

	public void testRmiProxyFactoryBeanWithRemoteException() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean() {
			protected Remote createRmiProxy() {
				return new RemoteBean();
			}
		};
		factory.setServiceInterface(IRemoteBean.class);
		factory.setServiceUrl("rmi://localhost:1090/test");
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof IRemoteBean);
		IRemoteBean proxy = (IRemoteBean) factory.getObject();
		try {
			proxy.setName("exception");
			fail("Should have thrown RemoteException");
		}
		catch (RemoteException ex) {
			// expected
		}
	}

	public void testRmiProxyFactoryBeanWithBusinessInterface() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean() {
			protected Remote createRmiProxy() {
				return new RemoteBean();
			}
		};
		factory.setServiceInterface(IBusinessBean.class);
		factory.setServiceUrl("rmi://localhost:1090/test");
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof IBusinessBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		assertFalse(proxy instanceof IRemoteBean);
		proxy.setName("myName");
		assertEquals(RemoteBean.name, "myName");
	}

	public void testRmiProxyFactoryBeanWithBusinessInterfaceAndRemoteException() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean() {
			protected Remote createRmiProxy() {
				return new RemoteBean();
			}
		};
		factory.setServiceInterface(IBusinessBean.class);
		factory.setServiceUrl("rmi://localhost:1090/test");
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof IBusinessBean);
		IBusinessBean proxy = (IBusinessBean) factory.getObject();
		assertFalse(proxy instanceof IRemoteBean);
		try {
			proxy.setName("exception");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testHessianProxyFactoryBeanWithAccessError() throws Exception {
		HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
		try {
			factory.setServiceInterface(TestBean.class);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();
		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testBurlapProxyFactoryBeanWithAccessError() throws Exception {
		BurlapProxyFactoryBean factory = new BurlapProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();
		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}
	
	public void testRmiClientInterceptorRequiresUrl() throws Exception{
		RmiClientInterceptor client = new RmiClientInterceptor();
		client.setServiceInterface(IRemoteBean.class);

		try {
			client.afterPropertiesSet();
			fail("url isn't set, expected IllegalArgumentException");
		} 
		catch(IllegalArgumentException e){
			//expected
		}
	}
	
	public void testBogusRmiServiceExporter() throws Exception{
		RmiServiceExporter exporter = new RmiServiceExporter();
		exporter.setServiceName("bogusService");
		exporter.setServicePort(9999);
		exporter.setRegistryPort(8888);
		RemoteBean testBean = new RemoteBean();
		exporter.setServiceInterface(IRemoteBean.class);
		exporter.setService(testBean);

		try {
			exporter.afterPropertiesSet();
			fail("calling an unregistered service, should have thrown StubNotFoundException");
		} 
		catch(StubNotFoundException e){
			//expected
		}

		//test the "service has no name" case
		exporter.setServiceName(null);

		try {
			exporter.afterPropertiesSet();
			fail("did not provide a name for the service, should have thrown IllegalArgumentException");
		} 
		catch(IllegalArgumentException illegalArg){
			//expected
		}
	}



	public static interface IBusinessBean {

		public void setName(String name);

	}


	public static interface IRemoteBean extends Remote {

		public void setName(String name) throws RemoteException;

	}


	public static class RemoteBean implements IRemoteBean, Stub {

		private static String name;
		private static Map properties;

		public RemoteBean() {
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
