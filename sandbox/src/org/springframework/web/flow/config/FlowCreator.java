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
package org.springframework.web.flow.config;

import org.springframework.web.flow.Flow;

/**
 * Factory that encapsulates the creation of a <code>Flow</code> instance. The
 * instance may be <code>Flow</code>, in the default case, or a custom
 * extension.
 * 
 * This interface is useful when you require specific <code>Flow</code>
 * specialization that are shared between different <code>FlowBuilder</code>
 * implementations.
 * 
 * @author Keith Donald
 */
public interface FlowCreator {

	/**
	 * Factory method that create the <code>Flow</code> instance with the
	 * specified id.
	 * @param id The flow identifier
	 * @return The <code>Flow</code> (or a custom specialization of Flow)
	 */
	public Flow createFlow(String id);
}