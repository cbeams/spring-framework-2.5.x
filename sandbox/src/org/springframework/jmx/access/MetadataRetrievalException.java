/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jmx.access;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown if an exception is encountered when trying to retreive
 * MBean metadata.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see MBeanClientInterceptor
 * @see MBeanProxyFactoryBean
 */
public class MetadataRetrievalException extends NestedRuntimeException {

	/**
	 * Creates a new instance of <code>MetadataRetrievalException</code> with the
	 * specified error message.
	 *
	 * @param msg the error message.
	 */
	public MetadataRetrievalException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new instance of <code>MetadataRetrievalException</code> with the
	 * specified error message and root cause.
	 *
	 * @param msg the error message.
	 * @param ex the root cause.
	 */
	public MetadataRetrievalException(String msg, Throwable ex) {
		super(msg, ex);
	}

}