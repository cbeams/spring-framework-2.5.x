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
 * Created on 23-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.context.support.DefaultOsgiBundleXmlApplicationContextFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContextFactory;
import org.springframework.osgi.service.OsgiServiceUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * OSGi bundle activator class to be used as the bundle activator for OSGi
 * bundles using Spring services. When the bundle is activated it creates an
 * application context and destroys it when the bundled is stopped.
 * 
 * By default the ContextLoaderBundleActivator will look for an application
 * context file in the location
 * /META-INF/&lt;bundle-symbolic-name&gt;-context.xml. You can override this
 * default behaviour by adding a bundle manifest header entry of the form
 * 
 * Spring-Context: &lt;comma-delimited list of context file locations&gt;
 * 
 * The manifest entry may contain any number of resource paths, separated by any
 * number of commas and spaces.
 * 
 * TODO: support parent application context via additional header giving name of
 * parent application context service (by default this will be
 * <bundle-symbolic-name>-springApplicationContext.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class ContextLoaderBundleActivator implements BundleActivator {

	private static final String CONTEXT_LOCATION_HEADER = "Spring-Context";
	private static final String PARENT_CONTEXT_SERVICE_NAME_HEADER = "Spring-Parent-Context";
	private static final String CONTEXT_LOCATION_DELIMITERS = ", ";
	private static final String DEFAULT_CONTEXT_PREFIX = "/META-INF/";
	private static final String DEFAULT_CONTEXT_POSTFIX = "-context.xml";
	private static final String CONTEXT_OPTIONS = "Spring-Context-Options";
	/**
	 * is this really required? - if no parent is found, an exception is thrown
	 * anyway
	 */
	private static final String FAIL_FAST_OPTION = "honor-dependent-services";

	private OsgiBundleXmlApplicationContextFactory contextFactory = new DefaultOsgiBundleXmlApplicationContextFactory();
	private ConfigurableApplicationContext applicationContext;
	private ServiceReference parentServiceReference;
	private ServiceTracker serviceTracker;

	private static final Log log = LogFactory.getLog(ContextLoaderBundleActivator.class);

	/**
	 * BundleActivator.start
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Bundle myBundle = bundleContext.getBundle();
		log.info("starting bundle " + myBundle.getSymbolicName() + myBundle.getBundleId());
		String[] applicationContextLocations = getApplicationContextLocations(myBundle);
		ApplicationContext parent = getParentApplicationContext(bundleContext);
		this.applicationContext = this.contextFactory.createApplicationContext(parent, bundleContext,
				applicationContextLocations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (this.applicationContext != null) {
			this.applicationContext.close();
		}
		if (this.parentServiceReference != null) {
			bundleContext.ungetService(this.parentServiceReference);
		}

		if (this.serviceTracker != null)
			serviceTracker.close();
	}

	/**
	 * Search for the Spring-Parent-Context application context.
	 * 
	 * @param context
	 * @return
	 */
	protected ApplicationContext getParentApplicationContext(final BundleContext context) {
		String parentContextServiceName = (String) context.getBundle().getHeaders().get(
				PARENT_CONTEXT_SERVICE_NAME_HEADER);
		if (parentContextServiceName == null) {
			if (log.isDebugEnabled())
				log.debug("no need to look for a parent context");

			return null;
		}
		else {
			if (log.isDebugEnabled())
				log.debug("looking for a parent context...");

			// try to find the service
			String filter = "(" + OsgiBundleXmlApplicationContext.APPLICATION_CONTEXT_SERVICE_NAME_HEADER + "="
					+ parentContextServiceName + ")";

			parentServiceReference = OsgiServiceUtils.getService(context, ApplicationContext.class, filter);

			// TODO: register as service listener..., probably in a proxy to the
			// app context
			// that we create here and return instead.
			// Costin: done, should be verified though.

			return createApplicationContextProxy(context, parentServiceReference);
		}
	}

	/**
	 * Create a proxy around the target application context.
	 * 
	 * @param parent
	 * @return
	 */
	protected ApplicationContext createApplicationContextProxy(BundleContext context, ServiceReference serviceReference) {

		LookupApplicationContextInvocationHandler handler = new LookupApplicationContextInvocationHandler(context,
				serviceReference, serviceTracker);

		// TODO: interfaces are detected dynamically - is this dangerous (for
		// example if the parent context changes)
		// As the child depends on it, recreating the parent context should
		// trigger the whole process again.
		ApplicationContext target = handler.getTarget();
		Class[] ifaces = (target == null ? new Class[] { ApplicationContext.class }
				: ClassUtils.getAllInterfaces(target));

		return (ApplicationContext) Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
	}

	/**
	 * Retrieves the org.springframework.context manifest header attribute and
	 * parses it to create a String[] of resource names for creating the
	 * application context.
	 * 
	 * If the org.springframework.context header is not present, the default
	 * <bundle-symbolic-name>-context.xml file will be returned.
	 */
	protected String[] getApplicationContextLocations(Bundle bundle) {
		Dictionary manifestHeaders = bundle.getHeaders();
		String contextLocationsHeader = (String) manifestHeaders.get(CONTEXT_LOCATION_HEADER);
		if (contextLocationsHeader != null) {
			// (Dictionary does not offer a "containsKey" operation)
			return addBundlePrefixTo(StringUtils.tokenizeToStringArray(contextLocationsHeader,
					CONTEXT_LOCATION_DELIMITERS));
		}
		else {
			String defaultName = DEFAULT_CONTEXT_PREFIX + bundle.getSymbolicName() + DEFAULT_CONTEXT_POSTFIX;
			return addBundlePrefixTo(new String[] { defaultName });
		}
	}

	/**
	 * add the "bundle:" prefix to the resource location paths in the given
	 * argument. This ensures that only this bundle will be searched for
	 * matching resources.
	 * 
	 * Modifies the argument in place and returns it.
	 */
	private String[] addBundlePrefixTo(String[] resourcePaths) {
		for (int i = 0; i < resourcePaths.length; i++) {
			resourcePaths[i] = OsgiBundleResourceLoader.BUNDLE_URL_PREFIX + resourcePaths[i];
		}
		return resourcePaths;
	}

	// for testing...
	protected void setApplicationContext(ConfigurableApplicationContext context) {
		this.applicationContext = context;
	}

	// for testing...
	protected void setParentServiceReference(ServiceReference ref) {
		this.parentServiceReference = ref;
	}

	// for testing...
	protected void setApplicationContextFactory(OsgiBundleXmlApplicationContextFactory factory) {
		this.contextFactory = factory;
	}

	/**
	 * Simple lookup proxy using a ServiceTracker underneath.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private static class LookupApplicationContextInvocationHandler implements InvocationHandler {
		private final ServiceReference serviceReference;
		private ApplicationContext target;

		public LookupApplicationContextInvocationHandler(final BundleContext context, ServiceReference serviceRef,
				ServiceTracker serviceTracker) {
			this.serviceReference = serviceRef;

			ApplicationContext parent = (ApplicationContext) context.getService(serviceReference);

			serviceTracker = new ServiceTracker(context, serviceReference, new ServiceTrackerCustomizer() {
				public Object addingService(ServiceReference ref) {
					if (log.isDebugEnabled())
						log.debug("parentApplicationContext has been discovered");

					// multiple parent contexts are already handled by
					// OsgiServiceUtils.getService()
					target = (ApplicationContext) context.getService(ref);
					return target;
				}

				public void modifiedService(ServiceReference ref, Object service) {
					if (log.isDebugEnabled())
						log.debug("parentApplicationContext has been modified");
					// TODO: should we refresh the child context (happens
					// automatically if the parent is refreshed)
				}

				public void removedService(ServiceReference ref, Object service) {
					if (log.isDebugEnabled())
						log.debug("parentApplicationContext has been removed");
					target = null;

					// TODO: should we close the child context?
				}
			});
		}

		/**
		 * Used to get the discovered target object (for example for detecting
		 * the implemented interfaces).
		 * 
		 * @return
		 */
		protected ApplicationContext getTarget() {
			return target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			if (methodName.equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (methodName.equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			try {
				if (target != null)
					return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			throw new UnsupportedOperationException("no parentApplicationContext in place");
		}
	}

	/**
	 * Close suppressing invocation handler - proxy used as a 'shield' against
	 * forbidden close inside an OSGi environment.
	 * 
	 * Not used at the moment.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private static class CloseSuppresingApplicationContextInvocationHandler implements InvocationHandler {
		private final ApplicationContext target;

		public CloseSuppresingApplicationContextInvocationHandler(ApplicationContext appContext) {
			this.target = appContext;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			// suppress close calls:
			// applicationContext close
			// Lifecycle interface
			// TODO: what about refresh() ?
			if (methodName.equals("close") || methodName.equals("stop"))
				return null;

			if (methodName.equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (methodName.equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
