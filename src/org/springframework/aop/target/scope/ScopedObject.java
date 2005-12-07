/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.aop.target.scope;

/**
 * Interface for use as an introduction for scoped objects.
 * Objects created from the ScopedProxyFactoryBean can be
 * cast to this interface, enabling their Handle and other
 * information to be obtained.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public interface ScopedObject extends ScopingConfig {

	Handle getHandle();
	
	/**
	 * Remove this object. No further calls may be made.
	 */
	void remove();
	
}
