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

import oracle.classloader.PolicyClassLoader;
import oracle.classloader.util.ClassPreprocessor;
import oracle.classloader.util.ClassPreprocessorSequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.instrument.classloading.AbstractLoadTimeWeaver;

/**
 * LoadTimeWeaver suitable for OC4J. Many thanks to <a href="mailto:mike.keith@oracle.com">Mike Keith</a> for assistance.
 * 
 * @author Costin Leau
 * @since 2.0
 * 
 */
public class OC4JLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private static final Log log = LogFactory.getLog(OC4JLoadTimeWeaver.class);
	private static boolean debug = log.isDebugEnabled();
	
	private PolicyClassLoader classLoader;

	public OC4JLoadTimeWeaver() {
		this.classLoader = fetchPolicyClassLoader(getContextClassLoader());
	}

	public OC4JLoadTimeWeaver(ClassLoader loader) {
		this.classLoader = fetchPolicyClassLoader(loader);
	}

	protected PolicyClassLoader fetchPolicyClassLoader(ClassLoader cl) {
		if (cl instanceof PolicyClassLoader)
			return (PolicyClassLoader) cl;
		throw new IllegalArgumentException("the contextClassLoader is not instance of "
				+ PolicyClassLoader.class.getName() + "; maybe a different LoadTimeWeaver should be used?");
	}

	public ClassLoader getInstrumentableClassLoader() {
		return classLoader;
	}

	public void addTransformer(ClassFileTransformer classFileTransformer) {
		OC4JClassPreprocessorAdapter processor = new OC4JClassPreprocessorAdapter(classFileTransformer);

		// Add the transformer, checking first to see if one is already present
		if (classLoader.getPreprocessor() == null) {
			// None, just set the ClassPreprocessor
			classLoader.setPreprocessor(processor);
			if (debug)
				log.debug("setting transformer " + processor);

		}
		else {
			// Need to create a ClassPreprocessorSequence wrapper
			classLoader.setPreprocessor(new ClassPreprocessorSequence(new ClassPreprocessor[] {
					classLoader.getPreprocessor(), processor }, 2));
			if (debug)
				log.debug("chaining transformer " + processor);
		}
	}

	@Override
	public ClassLoader getThrowawayClassLoader() {
		ClassLoader loader = classLoader.copy(false, true);
		return loader;
	}
}
