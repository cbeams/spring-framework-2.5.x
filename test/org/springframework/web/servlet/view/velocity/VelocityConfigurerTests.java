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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityInitializationException;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class VelocityConfigurerTests extends TestCase {

	public void testVelocityEngineFactoryBeanWithConfigLocation() {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setConfigLocation(new FileSystemResource("myprops.properties"));
		Properties props = new Properties();
		props.setProperty("myprop", "/mydir");
		vefb.setVelocityProperties(props);
		try {
			vefb.getObject();
			fail("Should have thrown VelocityInitializationException");
		}
		catch (VelocityInitializationException ex) {
			// expected
		}
	}

	public void testVelocityEngineFactoryBeanWithResourceLoaderPath() {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setResourceLoaderPath(new FileSystemResource("/mydir") {
			public boolean exists() {
				return true;
			}
		});
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals(new File("/mydir").getAbsolutePath(), ve.getProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH));
	}

	public void testVelocityConfigurer() {
		MockControl acControl = MockControl.createControl(ApplicationContext.class);
		ApplicationContext ac = (ApplicationContext) acControl.getMock();
		acControl.replay();

		VelocityConfigurer vc = new VelocityConfigurer();
		vc.setResourceLoaderPath(new FileSystemResource("/mydir") {
			public boolean exists() {
				return true;
			}
		});
		vc.setApplicationContext(ac);
		assertTrue(vc.getVelocityEngine() instanceof VelocityEngine);
		VelocityEngine ve = vc.getVelocityEngine();
		assertEquals(new File("/mydir").getAbsolutePath(), ve.getProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH));

		acControl.verify();
	}

	public void testVelocityConfigurerWithDefaultLocation() throws Exception {
		VelocityConfigurer vc = new VelocityConfigurer();
		String configLocation = VelocityConfigurer.DEFAULT_CONFIG_LOCATION;
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getResource(configLocation);
		wmc.setReturnValue(new InputStreamResource(new StringBufferInputStream("foo=bar"), "test"));
		wmc.replay();
		vc.setApplicationContext(wac);

		VelocityEngine ve = vc.getVelocityEngine();
		assertTrue(ve.getProperty("foo").equals("bar"));
		// Check the new VelocityEngine was inited
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
		wac.getResource(configLocation);
		wmc.setReturnValue(new InputStreamResource(new StringBufferInputStream("foo=bar"), "test"));
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
