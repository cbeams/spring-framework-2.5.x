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

package org.springframework.jmx.support;

/**
 * Interface for classes that host non-standard JMX adapters such as the HTTP adapter
 * supplied with the reference implementation.
 * @author Rob Harrop
 * @since 1.2
 */
public interface AdapterHost {

	/**
	 * Start the adapter.
	 */
	void start();

	/**
	 * Stop the adapter.
	 */
	void stop();

}
