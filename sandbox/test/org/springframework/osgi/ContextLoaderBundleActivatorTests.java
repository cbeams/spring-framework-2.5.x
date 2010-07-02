/*
 * Copyright 2002-2006 the original author or authors.
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
 *
 * Created on 25-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi;

import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.internal.ArrayMatcher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.context.ContextLoaderBundleActivator;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContextFactory;

/**
 * @author Adrian Colyer
 * @since 2.0
 */
public class ContextLoaderBundleActivatorTests extends TestCase {

	private MockControl mockContextControl;
	private BundleContext bundleContext;
	private MockControl mockBundleControl;
	private Bundle bundle;

	protected void setUp() throws Exception {
		super.setUp();
		this.mockContextControl = MockControl.createControl(BundleContext.class);
		this.bundleContext = (BundleContext) this.mockContextControl.getMock();
		this.mockBundleControl = MockControl.createControl(Bundle.class);
		this.bundle = (Bundle) this.mockBundleControl.getMock();
	}

	
	public void testGetApplicationContextLocationsNoHeader() {
		this.bundle.getHeaders();
		this.mockBundleControl.setReturnValue(new Properties());
		this.bundle.getSymbolicName();
		this.mockBundleControl.setReturnValue("symbolic-name");
		this.mockBundleControl.replay();
		String[] ret = new ContextLoaderBundleActivator() {
			public String[] getApplicationContextLocations(Bundle b) {
				return super.getApplicationContextLocations(b);
			};
		}.getApplicationContextLocations(this.bundle);
		assertEquals("1 location",1,ret.length);
		assertEquals("bundle:/META-INF/symbolic-name-context.xml",ret[0]);
		this.mockBundleControl.verify();
	}
	
	public void testGetApplicationContextLocationsWithHeader() {
		this.bundle.getHeaders();
		Properties headers = new Properties();
		headers.put("Spring-Context","/META-INF/applicationContext.xml /META-INF/securityContext.xml");
		this.mockBundleControl.setReturnValue(headers);
		this.mockBundleControl.replay();
		String[] ret = new ContextLoaderBundleActivator() {
			public String[] getApplicationContextLocations(Bundle b) {
				return super.getApplicationContextLocations(b);
			};
		}.getApplicationContextLocations(this.bundle);
		assertEquals("2 locations",2,ret.length);
		assertEquals("bundle:/META-INF/applicationContext.xml",ret[0]);
		assertEquals("bundle:/META-INF/securityContext.xml",ret[1]);
		this.mockBundleControl.verify();
	}
	
	public void testGetParentApplicationContextNoHeader() {
		this.bundleContext.getBundle();
		this.mockContextControl.setReturnValue(this.bundle);
		this.bundle.getHeaders();
		this.mockBundleControl.setReturnValue(new Properties());
		this.mockBundleControl.replay();
		this.mockContextControl.replay();
		ApplicationContext parent = new ContextLoaderBundleActivator() {
			public ApplicationContext getParentApplicationContext(BundleContext context) {
				return super.getParentApplicationContext(context);
			};
		}.getParentApplicationContext(this.bundleContext);
		assertNull("Should not have a parent context",parent);
		this.mockBundleControl.verify();
		this.mockContextControl.verify();
	}
	
	public void testGetParentApplicationContextWithHeader() throws Exception {
		this.bundleContext.getBundle();
		this.mockContextControl.setReturnValue(this.bundle);
		this.bundle.getHeaders();
		Properties headers = new Properties();
		headers.put("Spring-Parent-Context","parentContextName");
		this.mockBundleControl.setReturnValue(headers);
		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "(org.springframework.context.service.name=parentContextName)");
		ServiceReference sRef = getServiceReference();
		this.mockContextControl.setReturnValue(new ServiceReference[] {sRef});
		this.bundleContext.getService(sRef);
		final ApplicationContext appContext = (ApplicationContext) MockControl.createControl(ApplicationContext.class).getMock();
		this.mockContextControl.setReturnValue(appContext);
		
		this.mockContextControl.replay();
		this.mockBundleControl.replay();
		
		
		ApplicationContext ret = new ContextLoaderBundleActivator() {
			public  ApplicationContext getParentApplicationContext(BundleContext context) {
				return super.getParentApplicationContext(context);
			}

			protected ApplicationContext createApplicationContextProxy(BundleContext context, ServiceReference serviceReference) {
				return (ApplicationContext) context.getService(serviceReference);
			};
			
			
		}.getParentApplicationContext(this.bundleContext);
		
		this.mockContextControl.verify();
		this.mockBundleControl.verify();
		assertSame("should get app context",appContext,ret);
	}
	
	public void testStopBundle() throws Exception {
		MockControl appContextControl = MockControl.createControl(ConfigurableApplicationContext.class);
		final ConfigurableApplicationContext appContext = (ConfigurableApplicationContext) appContextControl.getMock();
		appContext.close();
		appContextControl.replay();
		
		final ServiceReference sRef = getServiceReference();
		this.bundleContext.ungetService(sRef);
		this.mockContextControl.setReturnValue(true);
		this.mockContextControl.replay();
	
		new ContextLoaderBundleActivator() {
			public void stop(BundleContext bc) throws Exception {
				setApplicationContext(appContext);
				setParentServiceReference(sRef);
				super.stop(bc);
			};
		}.stop(this.bundleContext);
		
		appContextControl.verify();
		this.mockContextControl.verify();		
	}
	
	public void testStartBundle() throws Exception {
		this.bundleContext.getBundle();
		this.mockContextControl.setReturnValue(this.bundle);
		this.bundle.getHeaders();
		this.mockBundleControl.setReturnValue(new Properties());
		this.bundle.getSymbolicName();
		this.mockBundleControl.setReturnValue("symbolic-name");

		this.bundleContext.getBundle();
		this.mockContextControl.setReturnValue(this.bundle);
		
		// used for logging
		this.mockBundleControl.expectAndReturn(this.bundle.getBundleId(), 123l);
		this.mockBundleControl.expectAndReturn(this.bundle.getSymbolicName(), "symbolic-name");
		
		this.bundle.getHeaders();
		this.mockBundleControl.setReturnValue(new Properties());

		this.mockBundleControl.replay();
		this.mockContextControl.replay();
		
		MockControl mockFactory = MockControl.createControl(OsgiBundleXmlApplicationContextFactory.class);
		final OsgiBundleXmlApplicationContextFactory factory = (OsgiBundleXmlApplicationContextFactory) mockFactory.getMock();
		
		factory.createApplicationContext(null, this.bundleContext, new String[] {"bundle:/META-INF/symbolic-name-context.xml"});
		mockFactory.setMatcher(new ArrayMatcher());
		mockFactory.setReturnValue(null);
		mockFactory.replay();
		
		new ContextLoaderBundleActivator() {
			public void start(BundleContext bc) throws Exception {
				setApplicationContextFactory(factory);
				super.start(bc);
			};
		}.start(this.bundleContext);
		
		this.mockBundleControl.verify();
		this.mockContextControl.verify();
		mockFactory.verify();
	}
	
	private ServiceReference getServiceReference() {
		MockControl sRefControl = MockControl.createNiceControl(ServiceReference.class);
		ServiceReference mock = (ServiceReference) sRefControl.getMock();
		sRefControl.expectAndReturn(mock.getProperty(Constants.SERVICE_ID), "123");
		sRefControl.replay();
		
		return mock;
	}
}
