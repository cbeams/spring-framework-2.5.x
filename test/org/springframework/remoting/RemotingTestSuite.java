package org.springframework.remoting;

import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.remoting.caucho.BurlapProxyFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

/**
 * @author Juergen Hoeller
 * @since 16.05.2003
 */
public class RemotingTestSuite extends TestCase {

	public void testRmiProxyFactoryBean() throws Exception {
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());

		RmiServiceExporter exporter = new RmiServiceExporter();
		exporter.setService(tb);
		exporter.setName("test");
		exporter.setPort(1099);
		exporter.afterPropertiesSet();

		RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("rmi://localhost:1099/test");
		factory.afterPropertiesSet();

		ITestBean proxy = (ITestBean) factory.getObject();
		proxy.setAge(99);
		assertEquals(99, proxy.getAge());
		assertEquals(99, tb.getAge());

		try {
			proxy.exceptional(new IllegalAccessException());
			fail("Should have thrown IllegalAccessException");
		}
		catch (IllegalAccessException ex) {
			// expected
		}
		catch (Throwable t) {
			fail("Should have thrown IllegalAccessException");
		}

		try {
			proxy.exceptional(new IllegalStateException());
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
		catch (Throwable t) {
			fail("Should have thrown IllegalStateException");
		}

		try {
			proxy.exceptional(new OutOfMemoryError());
			fail("Should have thrown OutOfMemoryError");
		}
		catch (OutOfMemoryError ex) {
			// expected
		}
		catch (Throwable t) {
			fail("Should have thrown OutOfMemoryError");
		}
	}

	public void testRmiProxyFactoryBeanWithAccessError() throws Exception {
		RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("rmi://localhosta/testbean");
		try {
			factory.afterPropertiesSet();
			fail("Should have thrown RemoteException");
		}
		catch (RemoteException ex) {
			// expected
			ex.printStackTrace();
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
		ITestBean bean = (ITestBean) factory.getObject();
		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

}
