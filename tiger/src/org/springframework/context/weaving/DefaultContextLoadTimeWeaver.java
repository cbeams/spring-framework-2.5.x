/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.context.weaving;

import java.lang.instrument.ClassFileTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver;
import org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver;
import org.springframework.instrument.classloading.oc4j.OC4JLoadTimeWeaver;
import org.springframework.instrument.classloading.weblogic.WebLogicLoadTimeWeaver;

/**
 * Default {@link LoadTimeWeaver} bean for use in an application context,
 * decorating an automatically detected internal <code>LoadTimeWeaver</code>.
 *
 * <p>Typically registered for the default bean name
 * "<code>loadTimeWeaver</code>"; the most convenient way to achieve this is
 * Spring's <code>&lt;context:load-time-weaver&gt;</code> XML tag.
 *
 * <p>This class implements a runtime environment check for obtaining the
 * appropriate weaver implementation: As of Spring 2.5, it detects Sun's
 * GlassFish, Oracle's OC4J, BEA's WebLogic 10,
 * {@link InstrumentationSavingAgent Spring's VM agent} and any
 * {@link ClassLoader} supported by Spring's {@link ReflectiveLoadTimeWeaver}
 * (for example the
 * {@link org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader}).
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public class DefaultContextLoadTimeWeaver implements LoadTimeWeaver, BeanClassLoaderAware {

	protected final Log logger = LogFactory.getLog(getClass());

	private LoadTimeWeaver loadTimeWeaver;


	public void setBeanClassLoader(ClassLoader classLoader) {
		LoadTimeWeaver serverSpecificLoadTimeWeaver = createServerSpecificLoadTimeWeaver(classLoader);
		if (serverSpecificLoadTimeWeaver != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Determined server-specific load-time weaver: " +
						serverSpecificLoadTimeWeaver.getClass().getName());
			}
			this.loadTimeWeaver = serverSpecificLoadTimeWeaver;
		}
		else if (InstrumentationSavingAgent.getInstrumentation() != null) {
			logger.info("Found Spring's JVM agent for instrumentation");
			this.loadTimeWeaver = new InstrumentationLoadTimeWeaver();
		}
		else {
			try {
				this.loadTimeWeaver = new ReflectiveLoadTimeWeaver();
				logger.info("Using a reflective load-time weaver for class loader: " +
						this.loadTimeWeaver.getInstrumentableClassLoader());
			}
			catch (IllegalStateException ex) {
				throw new IllegalStateException(ex.getMessage() + " Specify a custom LoadTimeWeaver " +
						"or start your Java virtual machine with Spring's agent: -javaagent:spring-agent.jar");
			}
		}
	}

	/*
	 * This method never fails, allowing to try other possible ways to use an
	 * server-agnostic weaver. This non-failure logic is required since
	 * determining a load-time weaver based on the ClassLoader name alone may
	 * legitimately fail due to other mismatches. Specific case in point: the
	 * use of WebLogicLoadTimeWeaver works for WLS 10 but fails due to the lack
	 * of a specific method (addInstanceClassPreProcessor) for any earlier
	 * versions even though the ClassLoader name is the same.
	 */
	protected LoadTimeWeaver createServerSpecificLoadTimeWeaver(ClassLoader classLoader) {
		try {
			if (classLoader.getClass().getName().startsWith("weblogic")) {
				return new WebLogicLoadTimeWeaver(classLoader);
			}
			else if (classLoader.getClass().getName().startsWith("oracle")) {
				return new OC4JLoadTimeWeaver(classLoader);
			}
			else if (isMatchingClassLoaderInHierarchy(classLoader, "com.sun.enterprise")) {
				return new GlassFishLoadTimeWeaver(classLoader);
			}
		}
		catch (IllegalStateException ex) {
			logger.info("Could not obtain server-specific LoadTimeWeaver: " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Try to find a ClassLoader with matching name in the entire ClassLoader hierarchy.
	 * Used for GlassFish detection, where the web app ClassLoader might be from the
	 * embedded Tomcat - but its parent is going to be a GlassFish ClassLoader.
	 */
	private boolean isMatchingClassLoaderInHierarchy(ClassLoader classLoader, String loaderName) {
		return (classLoader != null &&
				(classLoader.getClass().getName().startsWith(loaderName) ||
						isMatchingClassLoaderInHierarchy(classLoader.getParent(), loaderName)));
	}


	public void addTransformer(ClassFileTransformer transformer) {
		this.loadTimeWeaver.addTransformer(transformer);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.loadTimeWeaver.getInstrumentableClassLoader();
	}

	public ClassLoader getThrowawayClassLoader() {
		return this.loadTimeWeaver.getThrowawayClassLoader();
	}

}
