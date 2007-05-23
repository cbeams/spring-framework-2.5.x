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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver;
import org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver;
import org.springframework.instrument.classloading.oc4j.OC4JLoadTimeWeaver;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that builds a default
 * LoadTimeWeaver for use in an application context. Typically registered for
 * the default bean name "loadTimeWeaver"; the most convenient way to achieve
 * this is Spring's <code>&lt;context:load-time-weaver&gt;</code> XML tag.
 *
 * <p>This class implements a runtime environment check for obtaining the
 * appropriate weaver implementation: As of Spring 2.1, it detects Sun's GlassFish,
 * Oracle's OC4J, Spring's VM agent and any ClassLoader supported by Spring's
 * ReflectiveLoadTimeWeaver (e.g. the TomcatInstrumentableClassLoader).
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public class LoadTimeWeaverFactoryBean implements FactoryBean {

	private final LoadTimeWeaver loadTimeWeaver;


	public LoadTimeWeaverFactoryBean() {
		ClassLoader cl = ClassUtils.getDefaultClassLoader();
		if (cl.getClass().getName().startsWith("com.sun.enterprise")) {
			this.loadTimeWeaver = new GlassFishLoadTimeWeaver(cl);
		}
		else if (cl.getClass().getName().startsWith("oracle")) {
			this.loadTimeWeaver = new OC4JLoadTimeWeaver(cl);
		}
		else if (InstrumentationSavingAgent.getInstrumentation() != null) {
			this.loadTimeWeaver = new InstrumentationLoadTimeWeaver();
		}
		else {
			try {
				this.loadTimeWeaver = new ReflectiveLoadTimeWeaver();
			}
			catch (IllegalStateException ex) {
				throw new IllegalStateException(ex.getMessage() + " Specify a custom LoadTimeWeaver " +
						"or start your Java virtual machine with Spring's agent: -javaagent:spring-agent.jar");
			}
		}
	}


	public Object getObject() {
		return this.loadTimeWeaver;
	}

	public Class getObjectType() {
		return this.loadTimeWeaver.getClass();
	}

	public boolean isSingleton() {
		return true;
	}

}
