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
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.AttributeSource;

/**
 * Holder for a logical view name and the dynamic model data neccessary to
 * render it. It is expected that a client map this logical view descriptor to a
 * physical view template for rendering.
 * <p>
 * For readers familiar with Spring MVC, this class is very similiar to the
 * <code>ModelAndView</code> construct. This class is provided to prevent a
 * web flow dependency on Spring MVC.
 * 
 * @see org.springframework.web.flow.ViewDescriptorCreator
 * 
 * @author Keith Donald
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Erwin Vervaet
 */
public class ViewDescriptor implements AttributeSource, Serializable {

	/**
	 * The name of the view (or page) to render. 
	 */
	private String viewName;
	
	/**
	 * A map of the data available to the view for rendering.
	 */
	private Map model;
	
	/**
	 * Default constructor for bean style usage.
	 */
	public ViewDescriptor() {
	}

	/**
	 * Convenient constructor when there is no model data to expose. Can also be
	 * used in conjunction with <code>addObject</code>.
	 * @param viewName name of the view to render
	 * @see #addObject
	 */
	public ViewDescriptor(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Creates new ViewDescriptor given a view name and a model.
	 * @param viewName name of the view to render
	 * @param model Map of model names (Strings) to model objects (Objects).
	 *        Model entries may not be null, but the model Map may be null if
	 *        there is no model data.
	 */
	public ViewDescriptor(String viewName, Map model) {
		this.viewName = viewName;
		this.model = model;
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the view to render
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ViewDescriptor(String viewName, String modelName, Object modelObject) {
		this.viewName = viewName;
		addObject(modelName, modelObject);
	}

	/**
	 * Set a view name for this ViewDescriptor. Will override any pre-existing view
	 * name.
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Return the view name.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Return the model map. Never returns null. To be called by application
	 * code for modifying the model.
	 */
	public Map getModel() {
		if (this.model == null) {
			this.model = new HashMap(1);
		}
		return this.model;
	}

	/**
	 * Add an object to the model.
	 * @param modelName name of the object to add to the model
	 * @param modelObject object to add to the model, may not be null
	 * @return this ViewDescriptor, convenient to allow usages like
	 *         <code>return viewDesc.addObject("foo", bar);</code>
	 */
	public ViewDescriptor addObject(String modelName, Object modelObject) {
		getModel().put(modelName, modelObject);
		return this;
	}

	/**
	 * Add all entries contained in the provided map to the model.
	 * @param modelMap a map of modelName->modelObject pairs
	 * @return this ViewDescriptor, convenient to allow usages like
	 *         <code>return viewDesc.addObject("foo", bar);</code>
	 */
	public ViewDescriptor addAllObjects(Map modelMap) {
		getModel().putAll(modelMap);
		return this;
	}

	/**
	 * Clear the state of this ViewDescriptor object. The object will be empty
	 * afterwards.
	 * @see #isEmpty
	 */
	public void clear() {
		this.viewName = null;
		this.model = null;
	}

	/**
	 * Return whether this ViewDescriptor object is empty, i.e. whether it does
	 * not hold any view and does not contain a model.
	 * @see #clear
	 */
	public boolean isEmpty() {
		return (this.viewName == null && this.model == null);
	}
	
	// attribute source implementation
	
	public boolean containsAttribute(String attributeName) {
		return model.containsKey(attributeName);
	}
	
	public Object getAttribute(String attributeName) {
		return model.get(attributeName);
	}

}