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
import org.springframework.instrument.classloading.AspectJWeavingTransformer;
import org.springframework.instrument.classloading.InstrumentationRegistry;
import org.springframework.util.StringUtils;

/**
 * Extension of tomcat default classloader which adds instrumentation to loaded
 * classes without the need of using an agent.
 * 
 * @author Costin Leau
 * 
 */
public class TomcatInstrumentableClassLoader extends WebappClassLoader implements InstrumentationRegistry {

	// use an internal weavingTransformer.
	AspectJWeavingTransformer weavingTransformer;

	public TomcatInstrumentableClassLoader() {
		super();
		weavingTransformer = new AspectJWeavingTransformer();
	}

	public TomcatInstrumentableClassLoader(ClassLoader cl) {
		super(cl);
		weavingTransformer = new AspectJWeavingTransformer(cl);
	}

	public void addClassFileTransformer(ClassFileTransformer cft) {
		weavingTransformer.addClassFileTransformer(cft);
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
			byte[] transformed = weavingTransformer.transformIfNecessary(name, internalName, entry.binaryContent);
			entry.binaryContent = transformed;
		}
		return entry;
	}

}
