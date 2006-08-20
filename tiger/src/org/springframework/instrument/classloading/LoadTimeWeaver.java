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
 * Interface supporting adding one or more ClassFileTransformers
 * to a class loader - typically the current context class loader.
 * Implementations may of course provide their own class loader as well.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 */
public interface LoadTimeWeaver {

	/**
	 * Add a class file transformer to be applied by this load time weaver.
	 * @param transformer the class file transformer to register
	 */
	void addTransformer(ClassFileTransformer transformer) ;
	
	/**
	 * Return a class loader that supports instrumentation through AspectJ-style
	 * load time weaving based on user-defined ClassFileTransformers. May be the
	 * current class loader, or a class loader created by this LoadTimeWeaver.
	 */
	ClassLoader getInstrumentableClassLoader();
	
	/**
	 * Return a throwaway class loader, enabling classes to be loaded and inspected
	 * without affecting the parent class loader. Most <i>not</i> return the same as
	 * {@link #getInstrumentableClassLoader() getInstrumentableClassLoader()}.
	 * @return a temporary throwaway class loader. Should return a new
	 * instance for each call, with no existing state.
	 */
	ClassLoader getThrowawayClassLoader();

}
