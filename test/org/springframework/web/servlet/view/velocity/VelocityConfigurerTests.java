
package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.io.StringBufferInputStream;

import javax.servlet.ServletException;

import org.apache.velocity.app.VelocityEngine;
import org.easymock.MockControl;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.WebApplicationContext;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 */
public class VelocityConfigurerTests extends TestCase {

	/**
	 * Constructor for VelocityConfigurerTests.
	 * @param arg0
	 */
	public VelocityConfigurerTests(String arg0) {
		super(arg0);
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
		catch (ApplicationContextException ex) {
			ex.printStackTrace();
			//assertTrue(ex.getMessage().indexOf("velocity.properties") != -1);
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
		wac.getResourceBasePath();
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
		wac.getResourceBasePath();
		wmc.setReturnValue(null);
		wmc.replay();
		try {
			vc.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ace) {
			assertTrue(ace.getRootCause() == ex);
		}

		wmc.verify();
	}

}
