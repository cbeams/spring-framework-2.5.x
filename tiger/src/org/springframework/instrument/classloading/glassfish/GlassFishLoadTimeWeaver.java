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
package org.springframework.instrument.classloading.glassfish;

import java.lang.instrument.ClassFileTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.sun.enterprise.loader.InstrumentableClassLoader;

/**
 * Glassfish specific LTW. Uses the application server enhancing capabilities,
 * to execute
 * 
 * @author Costin Leau
 * 
 */
public class GlassFishLoadTimeWeaver implements LoadTimeWeaver {

	private static final Log log = LogFactory.getLog(GlassFishLoadTimeWeaver.class);
	private InstrumentableClassLoader classLoader;

	public GlassFishLoadTimeWeaver() {
		this(ClassUtils.getDefaultClassLoader());
	}

	public GlassFishLoadTimeWeaver(ClassLoader loader) {
		Assert.notNull(loader, "parameter cannot be null");
		InstrumentableClassLoader cl = determineClassLoader(loader);

		if (cl == null)
			throw new IllegalArgumentException(loader + " and its parents are not suitable classloaders; an "
					+ InstrumentableClassLoader.class.getName() + " implementation required");

		this.classLoader = cl;

	}

	protected InstrumentableClassLoader determineClassLoader(ClassLoader loader) {
		// detect transformation aware classloader by traversing the hierarchy
		// (as in GlassFish, Spring can be loaded by the WebappClassLoader)
		for (ClassLoader cl = loader; cl != null; cl = cl.getParent()) {

			if (log.isDebugEnabled())
				log.debug("inspecting classloader " + cl);

			if (cl instanceof InstrumentableClassLoader) {
				if (log.isDebugEnabled())
					log.debug("found instrumentable classloader " + cl);
				return (InstrumentableClassLoader) cl;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver#addTransformer(java.lang.instrument.ClassFileTransformer)
	 */
	public void addTransformer(ClassFileTransformer transformer) {
		classLoader.addTransformer(new ClassTransformerAdapter(transformer));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver#getInstrumentableClassLoader()
	 */
	public ClassLoader getInstrumentableClassLoader() {
		return (ClassLoader) classLoader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver#getThrowawayClassLoader()
	 */
	public ClassLoader getThrowawayClassLoader() {
		return classLoader.copy();
	}

}
