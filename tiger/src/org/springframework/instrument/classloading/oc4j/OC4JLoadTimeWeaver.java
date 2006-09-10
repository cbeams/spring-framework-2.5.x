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

package org.springframework.instrument.classloading.oc4j;

import java.lang.instrument.ClassFileTransformer;

import oracle.classloader.util.ClassLoaderUtilities;

import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * LoadTimeWeaver implementation for OC4J's instrumentable ClassLoader.
 *
 * <p><b>NOTE:</b> Requires Oracle OC4J version 10.1.3.1 or higher.
 *
 * <p>Many thanks to <a
 * href="mailto:mike.keith@oracle.com">Mike Keith</a>
 * for his assistance.
 *
 * @author Costin Leau
 * @since 2.0
 */
public class OC4JLoadTimeWeaver implements LoadTimeWeaver {

	private final ClassLoader classLoader;


	/**
	 * Create a new OC4JLoadTimeWeaver for the current context class loader.
	 */
	public OC4JLoadTimeWeaver() {
		this.classLoader = ClassUtils.getDefaultClassLoader();
	}

	/**
	 * Create a new OC4JLoadTimeWeaver for the given class loader.
	 * @param classLoader the ClassLoader to delegate to for weaving
	 */
	public OC4JLoadTimeWeaver(ClassLoader classLoader) {
		Assert.notNull(classLoader, "ClassLoader must not be null");
		this.classLoader = classLoader;
	}


	public void addTransformer(ClassFileTransformer classFileTransformer) {
		// Since OC4J 10.1.3's PolicyClassLoader is going to be removed,
		// we rely on the ClassLoaderUtilities API instead.
		OC4JClassPreprocessorAdapter processor = new OC4JClassPreprocessorAdapter(classFileTransformer);
		ClassLoaderUtilities.addPreprocessor(this.classLoader, processor);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

	public ClassLoader getThrowawayClassLoader() {
		return ClassLoaderUtilities.copy(this.classLoader);
	}

}
