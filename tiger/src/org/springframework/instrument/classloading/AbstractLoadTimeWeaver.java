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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract implementation of the LoadTimeWeaver interface
 * providing access to the current ClassLoader and
 * defining a throwaway class loader
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractLoadTimeWeaver implements LoadTimeWeaver {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @return the current class loader
	 */
	protected ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public ClassLoader getThrowawayClassLoader() {
		return new ThrowawayClassLoader(getContextClassLoader());
	}

}
