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
 * Interface which allows to work with the classloader without holding any hard reference.
 * This way, super classes that are loaded in other classloader but are not visible inside webapps
 * can be handled in a normal way.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public interface InstrumentedClassLoader {
	
	boolean isAspectJWeavingEnabled();
	void addTransformer(ClassFileTransformer cft);
	void addClassNameToExcludeFromUndelegation(String className);

}
