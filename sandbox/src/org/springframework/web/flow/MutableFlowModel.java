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
package org.springframework.web.flow;

import java.util.Map;

import org.springframework.binding.AttributeSetter;

/**
 * Extension of flow model allowing for mutable operations. If you don't need
 * mutability, pass the immutable {@link FlowModel} interface around instead.
 * <p>
 * The attributes stored in the flow model ("flow scope") are changed using this
 * interface. Each flow model is associated with a exactly one flow session in
 * an ongoing flow execution.
 * <p>
 * Implementers of this interface (e.g. the flow session) can have an <i>active
 * transaction</i> and provide methods to start and stop transactions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface MutableFlowModel extends FlowModel, AttributeSetter {

	/**
	 * Perform a bulk-set operation on a number of attributes.
	 * @param attributes The map of attributes (name=value pairs).
	 */
	public void setAttributes(Map attributes);

	/**
	 * Remove the specified attribute, if it exists.
	 * @param attributeName The attribute name.
	 */
	public void removeAttribute(String attributeName);

	/**
	 * Start a new transaction on this model.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction on this model.
	 */
	public void endTransaction();
}