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

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.closure.Constraint;

/**
 * An interface that provides access to the data model of an executing flow
 * session. All attributes in the flow model are in "flow scope"; that is, they
 * exist for the life of a flow session and will be cleaned up when the flow
 * session ends. All attributes in the flow model are automatically exported for
 * convenient access by the views.
 * <p>
 * This is a simple interface for accessing flow model attributes, typically
 * from within controller (action) code. This interface helps prevent accidental
 * misuse and manipulation of more enabling interfaces like <code>Map</code>,
 * for example, through better encapsulation. It also adds a number of helpful
 * methods related to data model access.
 * <p>
 * Note: The flow model should not be used as a general purpose cache, but
 * rather as a stateful context for data needed locally from within the
 * individual states of a flow. For example, it would be inappropriate to stuff
 * large collections of objects (like those returned to support a search results
 * view) into the flow model. Instead, put such result collections in the
 * request. 2nd level caches are much better cache solutions.
 * <p>
 * Implementers of this interface (e.g <code>FlowExecutionStack</code>,
 * <code>FlowSession</code>) may also support <b>transaction tokens </b> and
 * provide methods to check if an incoming HTTP request is participating in an
 * application transaction.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowModel {

	/**
	 * Get the attribute value associated with the provided name, returning
	 * <code>null</code> if not found.
	 * @param attributeName The attribute name
	 * @return The attribute value, or null if not found.
	 */
	public Object getAttribute(String attributeName);

	/**
	 * Get the attribute value associated with the provided name and make sure
	 * it is of the required type, returning <code>null</code> if not found.
	 * @param attributeName The attribute name
	 * @param requiredType The required type of the attribute's value, if the
	 *        value is present in the model
	 * @return The attribute value, or null if not found.
	 * @throws IllegalStateException A value was found but it was of the wrong
	 *         type.
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * Get the attribute value associated with the provided name, throwing an
	 * exception if no attribute exists with the provided name in the model.
	 * @param attributeName The attribute name
	 * @return The attribute value
	 * @throws IllegalStateException if no attribute value exists with the
	 *         provided attribute name.
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException;

	/**
	 * Get the attribute value associated with the provided name and make sure
	 * it is of the required type, throwing an exception if no attribute exists
	 * with the provided name in the model.
	 * @param attributeName The attribute name
	 * @param requiredType The expected attribute value type
	 * @return The attribute value
	 * @throws IllegalStateException if no attribute value exists with the
	 *         provided attribute name, or a value exists but not of the
	 *         required type.
	 */
	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * Assert that a value is present for the provided attribute name.
	 * @param attributeName The attribute Name
	 * @throws IllegalStateException The value is not present
	 */
	public void assertAttributePresent(String attributeName) throws IllegalStateException;

	/**
	 * Assert that a value is present for the provided attribute name of the
	 * type specified.
	 * @param attributeName The attribute name
	 * @param requiredType The required type
	 * @throws IllegalStateException The value is not present or it is of the
	 *         wrong type.
	 */
	public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException;

	/**
	 * Assert that given request is participating in the active transaction of
	 * the model.
	 * @param request the current HTTP request
	 * @param reset indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException The request is not participating in the
	 *         active transaction of the model or there is no transaction active
	 *         in the model
	 */
	public void assertInTransaction(HttpServletRequest request, boolean reset) throws IllegalStateException;

	/**
	 * Does the attribute by the provided name exist in this model?
	 * @param attributeName the attribute name
	 * @return true if so, false otherwise.
	 */
	public boolean containsAttribute(String attributeName);

	/**
	 * Does the attribute by the provided name and type exist in this model?
	 * @param attributeName the attribute name
	 * @param requiredType the attribute value type
	 * @return true if so, false otherwise.
	 */
	public boolean containsAttribute(String attributeName, Class requiredType);

	/**
	 * Is given request participating in the active transaction of the model?
	 * @param request the current HTTP request
	 * @param reset indicates whether or not the transaction should end after
	 *        checking it
	 * @return True when the request is participating in the active transaction
	 *         of the model, false otherwise
	 */
	public boolean inTransaction(HttpServletRequest request, boolean reset);

	/**
	 * Return a collection of attribute names indexed in this model.
	 * @return The attribute names.
	 */
	public Collection attributeNames();

	/**
	 * Return a collection of attribute values present in this model.
	 * @return The attribute values.
	 */
	public Collection attributeValues();

	/**
	 * Return a collection of Map.Entry objects for each name=value attribute
	 * pair in this model.
	 * @return The model entries.
	 */
	public Collection attributeEntries();

	/**
	 * Find a collection of all attribute entries that meet the specified
	 * criteria.
	 * @param criteria The criteria
	 * @return The entries that match the criteria.
	 */
	public Collection findAttributes(Constraint criteria);
	
	/**
	 * Returns the data model for this flow model, suitable for exporting to
	 * web views.
	 * @return Map of model attributes for this flow model.
	 */
	public Map getModel();
}