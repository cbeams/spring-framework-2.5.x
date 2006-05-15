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

/**
 * Load time weaver relying on Instrumentation.
 * Start with java agent, with JVM options like:
 * <code>
 * -javaagent:path/to/jpa-test-agent.jar
 * </code>
 * where jpa-test-agent.jar is the JAR file containing 
 * InstrumentationSavingAgent.
 * <p>
 * In Eclipse, for example, set the Run configuration's JVM
 * args to be of the form:
 * <code>
 * -javaagent:${project_loc}/lib/jpa-test-agent.jar
 * </code>
 * @see InstrumentationSavingAgent
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class InstrumentationLoadTimeWeaver extends AbstractLoadTimeWeaver {
	
	public InstrumentationLoadTimeWeaver() {
		// Always do AspectJ load time weaving
		//addClassFileTransformer(new ClassPreProcessorAgentAdapter());
	}

	/**
	 * We have the ability to weave the current class loader when starting the
	 * JVM in this way, so the instrumentable class loader will always be the
	 * current loader
	 */
	public ClassLoader getInstrumentableClassLoader() {
		return getContextClassLoader();
	}


	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		log.info("Installing " + classFileTransformer);
		Instrumentation instrumentation = InstrumentationSavingAgent.getInstrumentation();
		
		if (instrumentation == null) {
			throw new UnsupportedOperationException("Must start with Java agent to use InstrumentationLoadTimeWeaver. " +
									"See Spring JPA documentation");
		}
		
		instrumentation.addTransformer(classFileTransformer);
	}

}
