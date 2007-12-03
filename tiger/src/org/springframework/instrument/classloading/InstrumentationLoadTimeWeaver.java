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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link LoadTimeWeaver} relying on VM {@link Instrumentation}.
 *
 * <p>Start the JVM specifying the Java agent to be used, like as follows:
 *
 * <p><code class="code">-javaagent:path/to/spring-agent.jar</code>
 *
 * <p>where <code>spring-agent.jar</code> is a JAR file containing the
 * {@link InstrumentationSavingAgent} class.
 *
 * <p>In Eclipse, for example, set the "Run configuration"'s JVM args
 * to be of the form:
 *
 * <p><code class="code">-javaagent:${project_loc}/lib/spring-agent.jar</code>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see InstrumentationSavingAgent
 */
public class InstrumentationLoadTimeWeaver implements LoadTimeWeaver {

	private final ClassLoader classLoader;

	private final List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>(4);


	public InstrumentationLoadTimeWeaver() {
		this.classLoader = ClassUtils.getDefaultClassLoader();
	}

	public InstrumentationLoadTimeWeaver(ClassLoader classLoader) {
		Assert.notNull(classLoader, "ClassLoader must not be null");
		this.classLoader = classLoader;
	}


	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		getInstrumentation().addTransformer(transformer);
		this.transformers.add(transformer);
	}

	/**
	 * We have the ability to weave the current class loader when starting the
	 * JVM in this way, so the instrumentable class loader will always be the
	 * current loader.
	 */
	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

	/**
	 * This implementation always returns a {@link SimpleThrowawayClassLoader}.
	 */
	public ClassLoader getThrowawayClassLoader() {
		return new SimpleThrowawayClassLoader(getInstrumentableClassLoader());
	}

	/**
	 * Remove all registered transformers, in inverse order of registration.
	 */
	public void removeTransformers() {
		Instrumentation instrumentation = getInstrumentation();
		for (int i = this.transformers.size() - 1; i >= 0; i--) {
			instrumentation.removeTransformer(this.transformers.get(i));
		}
		this.transformers.clear();
	}

	/**
	 * Obtain the Instrumentation instance for the current VM, if available
	 * @return the Instrumentation instance (never <code>null</code>)
	 * @throws IllegalStateException if instrumentation is not available
	 */
	private Instrumentation getInstrumentation() {
		Instrumentation instrumentation = InstrumentationSavingAgent.getInstrumentation();
		if (instrumentation == null) {
			throw new IllegalStateException(
					"Must start with Java agent to use InstrumentationLoadTimeWeaver. See Spring documentation.");
		}
		return instrumentation;
	}

}
