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

/**
 * Extension of attributes accessor allowing for mutable operations; if you dont
 * need mutability, pass AttributesAccessor around instead--it's safer.
 * @author Keith Donald
 */
public interface MutableAttributesAccessor extends AttributesAccessor {

	/**
	 * Set the attribute with the provided name to the value provided.
	 * @param attributeName The attribute name
	 * @param attributeValue The attribute value
	 */
	public void setAttribute(String attributeName, Object attributeValue);

	/**
	 * Remove the specified attribute, if it exists.
	 * @param attributeName The attribute name.
	 */
	public void removeAttribute(String attributeName);

	/**
	 * Perform a bulk-set operation on a number of attributes.
	 * @param attributes The map of attributes (name=value pairs).
	 */
	public void setAttributes(Map attributes);
	
    /**
     * <p>Save a new transaction token in this model.
     * 
     * @param tokenName the key used to save the token in the model map
     */
    public void setTransactionToken(String tokenName);

    /**
     * <p>Reset the saved transaction token in this model. This
     * indicates that transactional token checking will not be needed
     * on the next request that is submitted.
     * 
     * @param tokenName the key used to save the token in the model map
     */
    public void clearTransactionToken(String tokenName);
}