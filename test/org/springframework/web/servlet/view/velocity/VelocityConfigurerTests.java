
package org.springframework.web.servlet.view.velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Properties;

import javax.servlet.ServletException;

import junit.framework.TestCase;
import org.apache.velocity.app.VelocityEngine;
import org.easymock.MockControl;

import org.springframework.context.ApplicationContext;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityInitializationException;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class VelocityConfigurerTests extends TestCase {

	public void testVelocityEngineFactoryBeanWithoutApplicationContext() {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		Properties props = new Properties();
		props.setProperty("myprop", "${app.root}/mydir");
		vefb.setVelocityProperties(props);
		vefb.setAppRootMarker("${app.root}");
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals("${app.root}/mydir", ve.getProperty("myprop"));
	}

	public void testVelocityEngineFactoryBeanWithConfigLocationWithoutApplicationContext() {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setConfigLocation("myprops.properties");
		Properties props = new Properties();
		props.setProperty("myprop", "${app.root}/mydir");
		vefb.setVelocityProperties(props);
		vefb.setAppRootMarker("${app.root}");
		try {
			vefb.getObject();
			fail("Should have thrown VelocityInitializationException");
		}
		catch (VelocityInitializationException ex) {
			// expected
		}
	}

	public void testVelocityEngineFactoryBeanWithApplicationContext() {
		MockControl acControl = MockControl.createControl(ApplicationContext.class);
		ApplicationContext ac = (ApplicationContext) acControl.getMock();
		File resourceBase = new File("C:/mybase");
		ac.getResourceBase();
		acControl.setReturnValue(resourceBase);
		acControl.replay();

		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		Properties props = new Properties();
		props.setProperty("myprop", "${app.root}/mydir");
		vefb.setVelocityProperties(props);
		vefb.setAppRootMarker("${app.root}");
		vefb.setApplicationContext(ac);
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals(resourceBase.getAbsolutePath() + "/mydir", ve.getProperty("myprop"));

		acControl.verify();
	}

	public void testVelocityConfigurer() {
		MockControl acControl = MockControl.createControl(ApplicationContext.class);
		ApplicationContext ac = (ApplicationContext) acControl.getMock();
		File resourceBase = new File("C:/mybase");
		ac.getResourceBase();
		acControl.setReturnValue(resourceBase);
		acControl.replay();

		VelocityConfigurer vc = new VelocityConfigurer();
		Properties props = new Properties();
		props.setProperty("myprop", "${app.root}/mydir");
		vc.setVelocityProperties(props);
		vc.setAppRootMarker("${app.root}");
		vc.setApplicationContext(ac);
		assertTrue(vc.getVelocityEngine() instanceof VelocityEngine);
		VelocityEngine ve = vc.getVelocityEngine();
		assertEquals(resourceBase.getAbsolutePath() + "/mydir", ve.getProperty("myprop"));

		acControl.verify();
	}

	public void testDefaultVelocityPropertiesLocationNotFound() throws Exception {
		VelocityConfigurer vc = new VelocityConfigurer();
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getResourceAsStream(VelocityConfigurer.DEFAULT_CONFIG_LOCATION);
		wmc.setReturnValue(null);
		wmc.replay();
		try {
			vc.setApplicationContext(wac);
			fail();
		}
		catch (VelocityInitializationException ex) {
			// expected
		}
		wmc.verify();
	}
	
	public void testDefaultVelocityPropertiesFoundButBasePathNull() throws Exception {
		testDefaultVelocityPropertiesFoundButBasePathNull(null);
	}
	
	public void testCustomVelocityPropertiesFoundButBasePathNull() throws Exception {
		testDefaultVelocityPropertiesFoundButBasePathNull("war/somewhere.properties");
	}
	
	private boolean inited = false;
	
	private void testDefaultVelocityPropertiesFoundButBasePathNull(String configLocation) throws Exception {
		inited = false;
		VelocityConfigurer vc = new VelocityConfigurer() {
			protected VelocityEngine newVelocityEngine() {
				inited = true;
				return super.newVelocityEngine();
			}
		};
		if (configLocation != null) {
			vc.setConfigLocation(configLocation);
		}
		else {
			// We expect the default
			configLocation = VelocityConfigurer.DEFAULT_CONFIG_LOCATION;
		}
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getResourceAsStream(configLocation);
		wmc.setReturnValue(new StringBufferInputStream("foo=bar"));
		wac.getResourceBase();
		wmc.setReturnValue(null);
		wmc.replay();
		vc.setApplicationContext(wac);

		VelocityEngine ve = vc.getVelocityEngine();
		assertTrue(ve.getProperty("foo").equals("bar"));
		// Check the new VelocityEngine was inited
		assertTrue(inited);
		wmc.verify();
	}
	
	public void testDefaultVelocityLocationInitThrowsServletException() throws Exception {
		testDefaultVelocityLocationInitThrowsException(new ServletException());
	}
	
	public void testDefaultVelocityLocationInitThrowsIOException() throws Exception {
		testDefaultVelocityLocationInitThrowsException(new IOException());
	}
	
	public void testDefaultVelocityLocationInitThrowsUnspecifiedException() throws Exception {
		testDefaultVelocityLocationInitThrowsException(new Exception());
	}
	
	/**
	 * Check for graceful error handling if setting a property on the VelocityEngine fails
	 * @throws Exception
	 */
	private void testDefaultVelocityLocationInitThrowsException(final Exception ex) throws Exception {
		final VelocityEngine ve = new VelocityEngine() {
			public void init() throws Exception {
				throw ex;
			}

		};
		VelocityConfigurer vc = new VelocityConfigurer() {
			protected VelocityEngine newVelocityEngine() {
				return ve;
			}
		};
		String configLocation = VelocityConfigurer.DEFAULT_CONFIG_LOCATION;
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getResourceAsStream(configLocation);
		wmc.setReturnValue(new StringBufferInputStream("foo=bar"));
		wac.getResourceBase();
		wmc.setReturnValue(null);
		wmc.replay();
		try {
			vc.setApplicationContext(wac);
			fail();
		}
		catch (VelocityInitializationException ace) {
			assertTrue(ace.getRootCause() == ex);
		}

		wmc.verify();
	}

}
