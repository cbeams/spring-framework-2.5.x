package org.springframework.remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.rpc.Stub;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.remoting.caucho.BurlapProxyFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * @author Juergen Hoeller
 * @since 16.05.2003
 */
public class RemotingTestSuite extends TestCase {

	public void testRmiProxyFactoryBean() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean() {
			protected Remote createRmiProxy() throws Exception {
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
			protected Remote createRmiProxy() throws Exception {
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
			protected Remote createRmiProxy() throws Exception {
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
			protected Remote createRmiProxy() throws Exception {
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
