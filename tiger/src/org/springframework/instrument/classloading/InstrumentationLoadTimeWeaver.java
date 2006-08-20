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

import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Load time weaver relying on Instrumentation.
 * Start with java agent, with JVM options like:
 * <code>
 * -javaagent:path/to/spring-agent.jar
 * </code>
 * where <code>spring-agent.jar</code> is a JAR file
 * containing the InstrumentationSavingAgent class.
 *
 * <p>In Eclipse, for example, set the Run configuration's JVM
 * args to be of the form:
 * <code>
 * -javaagent:${project_loc}/lib/spring-agent.jar
 * </code>
 *
 * @author Rod Johnson
 * @since 2.0
 * @see InstrumentationSavingAgent
 */
public class InstrumentationLoadTimeWeaver implements LoadTimeWeaver {

	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		Instrumentation instrumentation = InstrumentationSavingAgent.getInstrumentation();
		if (instrumentation == null) {
			throw new IllegalStateException(
					"Must start with Java agent to use InstrumentationLoadTimeWeaver. See Spring documentation.");
		}
		instrumentation.addTransformer(transformer);
	}

	/**
	 * We have the ability to weave the current class loader when starting the
	 * JVM in this way, so the instrumentable class loader will always be the
	 * current loader.
	 */
	public ClassLoader getInstrumentableClassLoader() {
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * This implementation always returns a SimpleThrowawayClassLoader.
	 * @see SimpleThrowawayClassLoader
	 */
	public ClassLoader getThrowawayClassLoader() {
		return new SimpleThrowawayClassLoader(getInstrumentableClassLoader());
	}

}
