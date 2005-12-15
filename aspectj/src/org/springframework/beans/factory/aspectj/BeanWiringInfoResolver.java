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

package org.springframework.beans.factory.aspectj;

/**
 * Interface to be implemented by objects than can resolve bean name
 * information, given a newly instantiated object. Invocations to the
 * resolve() method on this interface will be driven by the AspectJ pointcut
 * in the relevant concrete aspect. Metadata resolution strategy can be
 * pluggable, but a good default, in the canonical CLASSNAME_WIRING_INFO_RESOLVER
 * instance, is to return the FQN.
 * 
 * @author Rod Johnson
 */
public interface BeanWiringInfoResolver {
	
	/**
	 * Resolve the BeanWiringInfo for this instance, or null if not found
	 * @param instance bean instance to resolve info for
	 * @return BeanWiringInfo, or null if not found
	 */
	BeanWiringInfo resolve(Object instance);
	
	/** 
	 * Simple  default implementation, which looks for a bean with the same name as the
	 * FQN. This is the default name of the bean in a Spring XML file if the id
	 * attribute is not used.
	 */
	BeanWiringInfoResolver CLASSNAME_WIRING_INFO_RESOLVER = new BeanWiringInfoResolver() {
		public BeanWiringInfo resolve(Object instance) {
			return new BeanWiringInfo(instance.getClass().getName());
		}
	};

}
