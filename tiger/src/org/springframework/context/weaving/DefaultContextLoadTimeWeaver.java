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

	private LoadTimeWeaver loadTimeWeaver;


	public void setBeanClassLoader(ClassLoader classLoader) {
		try {
			LoadTimeWeaver serverSpecificLoadTimeWeaver = createServerSpecificLoatTimeWeaver(classLoader);
			if (serverSpecificLoadTimeWeaver != null) {
				this.loadTimeWeaver = serverSpecificLoadTimeWeaver;
			}
			else if (InstrumentationSavingAgent.getInstrumentation() != null) {
				this.loadTimeWeaver = new InstrumentationLoadTimeWeaver();
			}
			else {
				this.loadTimeWeaver = new ReflectiveLoadTimeWeaver();
			}
		}
		catch (IllegalStateException ex) {
			throw new IllegalStateException(ex.getMessage() + " Specify a custom LoadTimeWeaver " +
					"or start your Java virtual machine with Spring's agent: -javaagent:spring-agent.jar");
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Failed to build appropriate LoadTimeWeaver - " +
					"make sure that that all required classes are on the classpath: " + ex);
		}
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

	/*
	 * This method never fails allowing to try other possible ways to use an
	 * server-agnostic weaver. This non-failure logic is required since
	 * determining load-time weaver based on classloader name alone may
	 * legitimately fail due to other mismatches. Specific case in point: the
	 * use of WebLogicLoadTimeWeaver works for wls10 but fails due to the lack
	 * of a specific method (addInstanceClassPreProcessor()) for any earlier
	 * versions even though the classloader name is the same.
	 */
	private LoadTimeWeaver createServerSpecificLoatTimeWeaver(ClassLoader classLoader) {
		try {
			String loaderClassName = classLoader.getClass().getName();
			if (loaderClassName.startsWith("com.sun.enterprise")) {
				return new GlassFishLoadTimeWeaver(classLoader);
			}
			else if (loaderClassName.startsWith("oracle")) {
				return new OC4JLoadTimeWeaver(classLoader);
			}
			else if (loaderClassName.startsWith("weblogic")) {
				return new WebLogicLoadTimeWeaver(classLoader);
			}
			else {
				return null;
			}
		}
		catch (IllegalStateException ex) {
			return null;
		}
	}
}
