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

package org.springframework.instrument.classloading.support;

import java.lang.instrument.ClassFileTransformer;

/**
 * LoadTimeWeaver that holds a narrow reference to the internal class
 * loader delegate. Such a class is useful when the container class loader
 * allows the interface to be loaded by the same class loader (the web
 * application has access to the classes loaded by the parent). This class
 * should be always used if possible instead of ReflectiveLoadTimeWeaver
 * since it avoids the reflection mechanism.
 *
 * <p>Mainly intended for use in simple environments, such as an IDE.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class SimpleLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private final SimpleInstrumentableClassLoader classLoader;

	
	public SimpleLoadTimeWeaver() {
		this.classLoader = new SimpleInstrumentableClassLoader(getContextClassLoader());
	}

	public SimpleLoadTimeWeaver(SimpleInstrumentableClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		this.classLoader.addClassFileTransformer(classFileTransformer);
	}

	public ClassLoader getInstrumentableClassLoader() {
		return this.classLoader;
	}

}
