/*
 * Copyright 2002-2007 the original author or authors.
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
 * Defines the contract for adding one or more
 * {@link ClassFileTransformer ClassFileTransformers} to a {@link ClassLoader}
 * - typically the current context <code>ClassLoader</code>.
 * 
 * <p>Implementations may of course provide their own <code>ClassLoader</code>
 * as well.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @see java.lang.instrument.ClassFileTransformer
 * @since 2.0
 */
public interface LoadTimeWeaver {

	/**
	 * Add a <code>ClassFileTransformer</code> to be applied by this
	 * <code>LoadTimeWeaver</code>.
	 * @param transformer the <code>ClassFileTransformer</code> to add
	 */
	void addTransformer(ClassFileTransformer transformer);
	
	/**
	 * Return a <code>ClassLoader</code> that supports instrumentation
	 * through AspectJ-style load-time weaving based on user-defined
	 * {@link ClassFileTransformer ClassFileTransformers}.
	 * <p>May be the current <code>ClassLoader</code>, or a
	 * <code>ClassLoader</code> created by this {@link LoadTimeWeaver}
	 * instance.
	 */
	ClassLoader getInstrumentableClassLoader();
	
	/**
	 * Return a throwaway <code>ClassLoader</code>, enabling classes to be
	 * loaded and inspected without affecting the parent
	 * <code>ClassLoader</code>.
	 * <p>Should <i>not</i> return the same instance of the {@link ClassLoader}
	 * returned from an invocation of {@link #getInstrumentableClassLoader()}.
	 * @return a temporary throwaway <code>ClassLoader</code>; should return a
	 * new instance for each call, with no existing state
	 */
	ClassLoader getThrowawayClassLoader();

}
