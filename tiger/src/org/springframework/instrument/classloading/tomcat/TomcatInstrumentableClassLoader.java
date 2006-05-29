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

import org.springframework.instrument.classloading.InstrumentationRegistry;
import org.springframework.instrument.classloading.support.WeavingTransformer;

/**
 * Extension of tomcat default classloader which adds instrumentation to
 * loaded classes without the need of using an agent.
 *
 * @author Costin Leau
 * @since 2.0
 */
public class TomcatInstrumentableClassLoader extends WebappClassLoader implements InstrumentationRegistry {

	/** Use an internal weavingTransformer */
	private final WeavingTransformer weavingTransformer;


	public TomcatInstrumentableClassLoader() {
		super();
		this.weavingTransformer = new WeavingTransformer();
	}

	public TomcatInstrumentableClassLoader(ClassLoader cl) {
		super(cl);
		this.weavingTransformer = new WeavingTransformer(cl);
	}


	public void addClassFileTransformer(ClassFileTransformer cft) {
		this.weavingTransformer.addClassFileTransformer(cft);
	}

	//
	// add hooks for adding transformation
	//

	@Override
	protected ResourceEntry findResourceInternal(String name, String path) {
		ResourceEntry entry = super.findResourceInternal(name, path);

		// postpone string parsing as much as possible (they are slow)
		if (entry != null && entry.binaryContent != null && path.endsWith(".class")) {
			byte[] transformed = weavingTransformer.transformIfNecessary(name, entry.binaryContent, null);
			entry.binaryContent = transformed;
		}
		return entry;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TomcatInstrumentedClassLoader\r\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
