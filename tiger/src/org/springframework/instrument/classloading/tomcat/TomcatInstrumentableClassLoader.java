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
package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;

import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.loader.WebappClassLoader;
import org.springframework.instrument.classloading.InstrumentableClassLoader;
import org.springframework.instrument.classloading.InstrumentedClassLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Extension of tomcat default classloader which adds instrumentation to loaded classes
 * without the need of using an agent.
 * 
 * @author Costin Leau
 * 
 * TODO: allow InstrumentableClassLoader to be loaded from the server classloader to avoid reflection.
 * TODO make Spring depend on this, but not the reverse.
 * put outside the core Spring code tree
 * 
 */
public class TomcatInstrumentableClassLoader extends WebappClassLoader implements InstrumentedClassLoader {

	// use an internal instrumentable classloader for delegation to reuse the logic
	InstrumentableClassLoader instrumentableClassLoader;


	public TomcatInstrumentableClassLoader() {
		super();
		instrumentableClassLoader = new InstrumentableClassLoader(ClassUtils.getDefaultClassLoader());
	}

	public TomcatInstrumentableClassLoader(ClassLoader cl) {
		super(cl);
		instrumentableClassLoader = new InstrumentableClassLoader(cl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.AbstractOverridingClassLoader#addClassNameToExcludeFromUndelegation(java.lang.String)
	 */
	public void addClassNameToExcludeFromUndelegation(String className) {
		instrumentableClassLoader.addClassNameToExcludeFromUndelegation(className);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentableClassLoader#addTransformer(java.lang.instrument.ClassFileTransformer)
	 */
	public void addTransformer(ClassFileTransformer cft) {
		instrumentableClassLoader.addTransformer(cft);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentableClassLoader#isAspectJWeavingEnabled()
	 */
	public boolean isAspectJWeavingEnabled() {
		return instrumentableClassLoader.isAspectJWeavingEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.lwt.InstrumentableClassLoader#setAspectJWeavingEnabled(boolean)
	 */
	public void setAspectJWeavingEnabled(boolean flag) {
		instrumentableClassLoader.setAspectJWeavingEnabled(flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.catalina.loader.WebappClassLoader#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TomcatInstrumentedClassLoader\r\n");
		sb.append(super.toString());
		return sb.toString();
	}
	
	//
	// add hooks for adding transformation
	//
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.catalina.loader.WebappClassLoader#findResourceInternal(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected ResourceEntry findResourceInternal(String name, String path) {

		ResourceEntry entry = super.findResourceInternal(name, path);
		
		// postpone string parsing as much as possible (they are slow) 
		if (entry != null && entry.binaryContent != null && path.endsWith(".class")) {
			String internalName = StringUtils.replace(name, ".", "/");
			byte[] transformed = instrumentableClassLoader.transformIfNecessary(name, internalName, entry.binaryContent);
			entry.binaryContent = transformed;
		}
		return entry;
	}


}
