/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.Conventions;
import org.springframework.util.Assert;

/**
 * Implementation of {@link java.util.Map} for use when building model data for use
 * with UI tools. Supports chained calls and generation of model attribute names.
 *
 * <p>This class serves as generic model holder for both Servlet and Portlet MVC,
 * but is not tied to either of those.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see Conventions#getVariableName
 * @see org.springframework.web.servlet.ModelAndView
 * @see org.springframework.web.portlet.ModelAndView
 */
public class ModelMap extends HashMap {

	/**
	 * Construct a new, empty <code>ModelMap</code>.
	 */
	public ModelMap() {
	}

	/**
	 * Construct a new <code>ModelMap</code> containing the supplied model object
	 * under the supplied name.
	 * @see #addObject(String, Object)
	 */
	public ModelMap(String modelName, Object modelObject) {
		addObject(modelName, modelObject);
	}

	/**
	 * Construct a new <code>ModelMap</code> containing the supplied model object.
	 * Uses attribute name generation to generate the key for the supplied model
	 * object.
	 * @see #addObject(Object)
	 */
	public ModelMap(Object modelObject) {
		addObject(modelObject);
	}


	/**
	 * Add the supplied <code>Object</code> under the supplied name.
	 * @param modelName the name of the model attribute (never <code>null</code>)
	 * @param modelObject the model attribute object (can be <code>null</code>)
	 */
	public ModelMap addObject(String modelName, Object modelObject) {
		Assert.notNull(modelName, "Model name must not be null");
		this.put(modelName, modelObject);
		return this;
	}

	/**
	 * Add the supplied <code>Object</code> to this <code>Map</code> used a
	 * {@link org.springframework.core.Conventions#getVariableName generated name}.
	 * <p/><emphasis>Note: Empty {@link Collection Collections} are not added to
	 * the model when using this method because we cannot correctly determine
	 * the true convention name. View code should check for <code>null</code> rather than
	 * for empty collections as is already done by JSTL tags</emphasis>.
	 * @param modelObject the model attribute object (never <code>null</code>)
	 */
	public ModelMap addObject(Object modelObject) {
		Assert.notNull(modelObject, "Model object must not be null");
		if (modelObject instanceof Collection && ((Collection) modelObject).isEmpty()) {
			return this;
		}
		return addObject(Conventions.getVariableName(modelObject), modelObject);
	}

	/**
	 * Copy all objects in the supplied <code>Map</code> into this <code>Map</code>.
	 */
	public ModelMap addAllObjects(Map objects) {
		if (objects != null) {
			this.putAll(objects);
		}
		return this;
	}

	/**
	 * Copy all objects in the supplied <code>Collection</code> into this <code>Map</code>,
	 * using attribute name generation for each element.
	 * @see #addObject(Object)
	 */
	public ModelMap addAllObjects(Collection objects) {
		if (objects != null) {
			for (Iterator it = objects.iterator(); it.hasNext();) {
				addObject(it.next());
			}
		}
		return this;
	}

}
