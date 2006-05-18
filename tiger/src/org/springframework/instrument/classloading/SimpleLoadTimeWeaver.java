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

/**
 * Intended for use only in simple environments, such as an IDE.
 * 
 * @author Rod Johnson
 */
public class SimpleLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private final InstrumentableClassLoader instrumentableClassLoader;

	
	public SimpleLoadTimeWeaver() {
		this.instrumentableClassLoader = new InstrumentableClassLoader(getContextClassLoader());
	}

//	public void setAspectJWeavingEnabled(boolean flag) {
//		if (flag == true && !instrumentableClassLoader.isAspectJWeavingEnabled()) {
//			instrumentableClassLoader.setAspectJWeavingEnabled(true);
//		}
//	}


	public ClassLoader getInstrumentableClassLoader() {
		return instrumentableClassLoader;
	}

	public void addClassFileTransformer(final ClassFileTransformer classFileTransformer) {
		this.instrumentableClassLoader.addTransformer(classFileTransformer);
	}

	//
	// public void addClassNameToExcludeFromWeaving(String className) {
	// instrumentableClassLoader.addClassNameToExcludeFromUndelegation(className);
	// }
	//	
	// public void setExplicitInclusions(Collection<String> explicitClassNames)
	// {
	// instrumentableClassLoader.setExplicitInclusions(explicitClassNames);
	// }

}
