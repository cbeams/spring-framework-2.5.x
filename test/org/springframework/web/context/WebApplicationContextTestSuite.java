package org.springframework.web.context;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.TestListener;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockServletContext;

/**
 * @author Rod Johnson
 */
public class WebApplicationContextTestSuite extends AbstractApplicationContextTests {

	private ServletContext servletContext;

	private RootWebApplicationContext root;

	protected ConfigurableApplicationContext createContext() throws Exception {
		InitAndIB.constructed = false;
		root = new XmlWebApplicationContext();
		MockServletContext sc = new MockServletContext("", "/org/springframework/web/context/WEB-INF/web.xml");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PARAM, "/org/springframework/web/context/WEB-INF/applicationContext.xml");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PREFIX_PARAM, "/org/springframework/web/context/WEB-INF/");
		this.servletContext = sc;
		root.initRootContext(sc);
		XmlWebApplicationContext wac = new XmlWebApplicationContext();
		wac.initNestedContext(sc, "test-servlet", root, null);
		return wac;
	}

	/**
	 * Overridden as we can't trust superclass method
	 * @see org.springframework.context.AbstractApplicationContextTests#testEvents()
	 */
	public void testEvents() throws Exception {
		TestListener listener = (TestListener) this.applicationContext.getBean("testListener");
		listener.zeroCounter();
		TestListener parentListener = (TestListener) this.applicationContext.getParent().getBean("parentListener");
		parentListener.zeroCounter();
		
		parentListener.zeroCounter();
		assertTrue("0 events before publication", listener.getEventCount() == 0);
		assertTrue("0 parent events before publication", parentListener.getEventCount() == 0);
		this.applicationContext.publishEvent(new MyEvent(this));
		assertTrue("1 events after publication, not " + listener.getEventCount(), listener.getEventCount() == 1);
		assertTrue("1 parent events after publication", parentListener.getEventCount() == 1);
	}

	public void testCount() {
		assertTrue("should have 14 beans, not "+ this.applicationContext.getBeanDefinitionCount(),
			this.applicationContext.getBeanDefinitionCount() == 14);
	}

	public void testWithoutMessageSource() throws Exception {
		MockServletContext sc = new MockServletContext("", "/org/springframework/web/context/WEB-INF/web.xml");
		sc.addInitParameter(XmlWebApplicationContext.CONFIG_LOCATION_PREFIX_PARAM, "/org/springframework/web/context/WEB-INF/");
		NestedWebApplicationContext wac = new XmlWebApplicationContext();
		wac.initNestedContext(sc, "testNamespace", null, null);
		try {
			wac.getMessage("someMessage", null, Locale.getDefault());
			fail("Should have thrown NoSuchMessageException");
		}
		catch (NoSuchMessageException ex) {
			// expected;
		}
		String msg = wac.getMessage("someMessage", null, "default", Locale.getDefault());
		assertTrue("Default message returned", "default".equals(msg));
	}

	public void testContextNesting() {
		ITestBean father = (ITestBean) this.applicationContext.getBean("father");
		assertTrue("Bean from root context", father != null);

		ITestBean rod = (ITestBean) this.applicationContext.getBean("rod");
		assertTrue("Bean from child context", "Rod".equals(rod.getName()));
		assertTrue("Bean has external reference", rod.getSpouse() == father);

		rod = (ITestBean) this.root.getBean("rod");
		assertTrue("Bean from root context", "Roderick".equals(rod.getName()));
	}

	public void testInitializingBeanAndInitMethod() throws Exception {
		assertFalse(InitAndIB.constructed);
		InitAndIB iib = (InitAndIB) this.applicationContext.getBean("init-and-ib");
		assertTrue(InitAndIB.constructed);
		assertTrue(iib.afterPropertiesSetInvoked && iib.initMethodInvoked);
		assertTrue(!iib.destroyed && !iib.customDestroyed);
		this.applicationContext.close();
		assertTrue(!iib.destroyed && !iib.customDestroyed);
		ConfigurableApplicationContext parent = (ConfigurableApplicationContext) this.applicationContext.getParent();
		parent.close();
		assertTrue(iib.destroyed && iib.customDestroyed);
		parent.close();
		assertTrue(iib.destroyed && iib.customDestroyed);
	}


	public static class InitAndIB implements InitializingBean, DisposableBean {

		public static boolean constructed;

		public boolean afterPropertiesSetInvoked, initMethodInvoked, destroyed, customDestroyed;

		public InitAndIB() {
			constructed = true;
		}

		public void afterPropertiesSet() {
			if (this.initMethodInvoked)
				fail();
			this.afterPropertiesSetInvoked = true;
		}

		/** Init method */
		public void customInit() throws ServletException {
			if (!this.afterPropertiesSetInvoked)
				fail();
			this.initMethodInvoked = true;
		}

		public void destroy() {
			if (this.customDestroyed)
				fail();
			if (this.destroyed) {
				throw new IllegalStateException("Already destroyed");
			}
			this.destroyed = true;
		}

		public void customDestroy() {
			if (!this.destroyed)
				fail();
			if (this.customDestroyed) {
				throw new IllegalStateException("Already customDestroyed");
			}
			this.customDestroyed = true;
		}
	}

}
