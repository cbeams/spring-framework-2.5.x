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
package org.springframework.web.flow.config;

import org.springframework.web.flow.TransitionCriteria;

/**
 * Interface for factories that can create <code>TransitionCriteria</code>
 * objects. This is typically used by a flow builder to create the transition
 * criteria objects that guard transition execution.
 * <p>
 * The factory is completely free in deciding how the result objects are
 * created: e.g. by parsing an expression, looking up an object in a bean
 * factory, ...
 * 
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Rob Harraop
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public interface TransitionCriteriaCreator {

	/**
	 * Create a new transition criteria object that will match on the
	 * specified encoded criteria expression.
	 * @param encodedCriteria the encoded criteria to match on
	 * @return the created transition criteria object
	 */
	public TransitionCriteria create(String encodedCriteria);

}
