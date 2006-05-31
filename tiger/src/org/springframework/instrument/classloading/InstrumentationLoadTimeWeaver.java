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
 */

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.instrument.InstrumentationSavingAgent;

/**
 * Load time weaver relying on Instrumentation.
 * Start with java agent, with JVM options like:
 * <code>
 * -javaagent:path/to/jpa-test-agent.jar
 * </code>
 * where <code>jpa-test-agent.jar</code> is a JAR file
 * containing the InstrumentationSavingAgent class.
 *
 * <p>In Eclipse, for example, set the Run configuration's JVM
 * args to be of the form:
 * <code>
 * -javaagent:${project_loc}/lib/jpa-test-agent.jar
 * </code>
 *
 * @author Rod Johnson
 * @since 2.0
 * @see InstrumentationSavingAgent
 */
public class InstrumentationLoadTimeWeaver extends AbstractLoadTimeWeaver {
	
	protected final Log logger = LogFactory.getLog(getClass());


	public InstrumentationLoadTimeWeaver() {
		// Always do AspectJ load time weaving
		//addTransformer(new ClassPreProcessorAgentAdapter());
	}


	/**
	 * We have the ability to weave the current class loader when starting the
	 * JVM in this way, so the instrumentable class loader will always be the
	 * current loader.
	 */
	public ClassLoader getInstrumentableClassLoader() {
		return getContextClassLoader();
	}

	public void addTransformer(ClassFileTransformer transformer) {
		if (logger.isInfoEnabled()) {
			logger.info("Installing " + transformer);
		}
		Instrumentation instrumentation = InstrumentationSavingAgent.getInstrumentation();
		if (instrumentation == null) {
			throw new UnsupportedOperationException(
					"Must start with Java agent to use InstrumentationLoadTimeWeaver. See Spring documentation.");
		}
		instrumentation.addTransformer(transformer);
	}

}
