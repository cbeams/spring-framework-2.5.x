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

package org.springframework.ui;

import org.springframework.util.Assert;
import org.springframework.util.ConventionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Implementation of {@link java.util.Map} for use when building model data for use
 * with UI tools. Supports chained calls and model parameter name generation.
 *
 * @author Rob Harrop
 * @see ConventionUtils#getVariableName(Object)
 * @since 2.0
 */
public class ModelMap extends HashMap {

	/**
	 * Construct a new, empty <code>ModelMap</code>.
	 */
	public ModelMap() {
	}

	/**
	 * Construct a new <code>ModelMap</code> containing the supplied model object.
	 * Uses parameter name generation to generate the key for the supplied model
	 * object.
	 * @see #addObject(Object)
	 */
	public ModelMap(Object modelObject) {
		addObject(modelObject);
	}

	/**
	 * Construct a new <code>ModelMap</code> containing the supplied model object under
	 * the supplied name.
	 * @see #addObject(String, Object)
	 */
	public ModelMap(String name, Object modelObject) {
		addObject(name, modelObject);
	}

	/**
	 * Adds the supplied <code>Object</code> to this <code>Map</code> used a
	 * {@link ConventionUtils#getVariableName generated name}.
	 * <p/><emphasis>Note: Empty {@link Collection Collections} are not added to
	 * the model when using this method because we cannot correctly determine
	 * the true convention name. View code should check for <code>null</code> rather than
	 * for empty collections as is already done by JSTL tags</emphasis>.
	 * @param modelObject the model parameter <code>Object</code>. Cannot be <code>null</code>.
	 */
	public ModelMap addObject(Object modelObject) {
		Assert.notNull(modelObject, "'modelObject' should not be null.");

		if (modelObject instanceof Collection && ((Collection) modelObject).isEmpty()) {
			return this;
		}

		return addObject(ConventionUtils.getVariableName(modelObject), modelObject);
	}

	/**
	 * Adds the supplied <code>Object</code> under the supplied name.
	 * @param name the name of the model parameter. Cannot be <code>null</code>.
	 * @param modelObject the model parameter object. Cannot be <code>null</code>.
	 */
	public ModelMap addObject(String name, Object modelObject) {
		Assert.notNull(name, "'name' should not be null.");
		Assert.notNull(modelObject, "'modelObject' should not be null.");
		this.put(name, modelObject);
		return this;
	}

	/**
	 * Copies all objects in the supplied <code>Map</code> into this <code>Map</code>.
	 */
	public ModelMap addAllObjects(Map objects) {
		if (objects != null) {
			this.putAll(objects);
		}
		return this;
	}

	/**
	 * Copies all objects in the supplied <code>Colletion</code> into this <code>Map</code>
	 * using parameter name generation for each element.
	 * @see #addObject(Object)
	 */
	public ModelMap addAllObjects(Collection objects) {
		if (objects != null) {
			for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
				addObject(iterator.next());
			}
		}
		return this;
	}
}
