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


/**
 * Delegation extension of InstrumentedClassLoader. Since most ClassLoaders have to extend the container classloader implementation
 * in which they are running, composition is required. This interface allows the composition classloader to expose the internal
 * delegate. 
 * 
 * @author Costin Leau
 * @since 2.0
 */
public interface DelegatedInstrumentedClassLoader extends InstrumentedClassLoader {

	/**
	 * Return the inner delegated classloader.
	 * 
	 * @return
	 */
	public ClassLoader getDelegatedClassLoader();
}
