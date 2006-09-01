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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.ClassUtils;

/**
 * LoadTimeWeaver suitable for OC4J. Many thanks to <a
 * href="mailto:mike.keith@oracle.com">Mike Keith</a> for assistance.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class OC4JLoadTimeWeaver implements LoadTimeWeaver {

	private static final Log log = LogFactory.getLog(OC4JLoadTimeWeaver.class);

	/**
	 * Since the PolicyClassLoader from 10.1.3 is going to move away we rely on
	 * the ClassLoaderUtilities.
	 */
	private ClassLoader classLoader;

	public OC4JLoadTimeWeaver() {
		this.classLoader = ClassUtils.getDefaultClassLoader();
	}

	public OC4JLoadTimeWeaver(ClassLoader loader) {
		this.classLoader = loader;
	}

	public void addTransformer(ClassFileTransformer classFileTransformer) {
		OC4JClassPreprocessorAdapter processor = new OC4JClassPreprocessorAdapter(classFileTransformer);

		ClassLoaderUtilities.addPreprocessor(classLoader, processor);

		if (log.isDebugEnabled())
			log.debug("added transformer " + processor);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return classLoader;
	}

	public ClassLoader getThrowawayClassLoader() {
		return ClassLoaderUtilities.copy(classLoader);
	}
}
