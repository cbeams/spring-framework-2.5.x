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

package org.springframework.web.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for both Model and View in the web MVC framework.
 * Note that these are entirely distinct. This class merely holds
 * both to make it possible for a controller to return both model
 * and view in a single return value.
 *
 * <p>Class to represent a model and view returned by a handler used
 * by a DispatcherServlet. The view can take the form of a reference
 * to a View object, or a String view name which will need to be
 * resolved by a ViewResolver object. The model is a Map, allowing
 * the use of multiple data objects keyed by name.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DispatcherServlet
 * @see ViewResolver
 * @see HandlerAdapter#handle
 * @see org.springframework.web.servlet.mvc.Controller#handleRequest
 */
public class ModelAndView {

	/** View if we hold an object reference */
	private View view;

	/**
	 * View name if we hold a view name that will be resolved
	 * by the DispatcherServlet via a ViewResolver
	 */
	private String viewName;

	/** Model */
	private Map model;


	/**
	 * Default constructor for bean-style usage: populating bean
	 * properties instead of passing in constructor arguments.
	 * @see #setView(View)
	 * @see #setViewName(String)
	 */
	public ModelAndView() {
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with <code>addObject</code>.
	 * @param view View object to render
	 * @see #addObject
	 */
	public ModelAndView(View view) {
		this.view = view;
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with <code>addObject</code>.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet
	 * @see #addObject
	 */
	public ModelAndView(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Creates new ModelAndView given a View object and a model.
	 * @param view View object to render
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be <code>null</code>, but the model Map
	 * may be <code>null</code> if there is no model data.
	 */
	public ModelAndView(View view, Map model) {
		this.view = view;
		this.model = model;
	}

	/** 
	 * Creates new ModelAndView given a view name and a model.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be <code>null</code>, but the model Map
	 * may be <code>null</code> if there is no model data.
	 */
	public ModelAndView(String viewName, Map model) {
		this.viewName = viewName;
		this.model = model;
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param view View object to render
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.viewName = viewName;
		addObject(modelName, modelObject);
	}


	/**
	 * Set a View object for this ModelAndView. Will override any
	 * pre-existing view name or View.
	 */
	public void setView(View view) {
		this.view = view;
		this.viewName = null;
	}

	/**
	 * Return the View object, or <code>null</code> if we are using a view name
	 * to be resolved by the DispatcherServlet via a ViewResolver.
	 */
	public View getView() {
		return view;
	}

	/**
	 * Set a view name for this ModelAndView, to be resolved by the
	 * DispatcherServlet via a ViewResolver. Will override any
	 * pre-existing view name or View.
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
		this.view = null;
	}

	/**
	 * Return the view name to be resolved by the DispatcherServlet
	 * via a ViewResolver, or <code>null</code> if we are using a View object.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Return whether we use a view reference, i.e. true if the
	 * view has been specified via a name to be resolved by the
	 * DispatcherServlet via a ViewResolver.
	 */
	public boolean isReference() {
		return (this.viewName != null);
	}

	/**
	 * Return the model map. May return null.
	 * Called by DispatcherServlet for evaluation of the model.
	 */
	protected Map getModelInternal() {
		return this.model;
	}

	/**
	 * Return the model map. Never returns null.
	 * To be called by application code for modifying the model.
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
	 * @param modelObject object to add to the model. May not be <code>null</code>.
	 * @return this ModelAndView, convenient to allow usages like
	 * return modelAndView.addObject("foo", bar);
	 */
	public ModelAndView addObject(String modelName, Object modelObject) {
		getModel().put(modelName, modelObject);
		return this;
	}

	/**
	 * Add all entries contained in the provided map to the model.
	 * @param modelMap a map of modelName->modelObject pairs
	 * @return this ModelAndView, convenient to allow usages like
	 * return modelAndView.addAllObjects(myModelMap);
	 */
	public ModelAndView addAllObjects(Map modelMap) {
		getModel().putAll(modelMap);
		return this;
	}

	/**
	 * Clear the state of this ModelAndView object.
	 * The object will be empty afterwards.
	 * <p>Can be used to suppress rendering of a given ModelAndView object
	 * in the <code>postHandle</code> method of a HandlerInterceptor.
	 * @see #isEmpty
	 * @see HandlerInterceptor#postHandle
	 */
	public void clear() {
		this.view = null;
		this.viewName = null;
		this.model = null;
	}

	/**
	 * Return whether this ModelAndView object is empty,
	 * i.e. whether it does not hold any view and does not contain a model.
	 * @see #clear
	 */
	public boolean isEmpty() {
		return (this.view == null && this.viewName == null && this.model == null);
	}


	/**
	 * Return diagnostic information about this model and view.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ModelAndView: ");
		if (isReference()) {
			buf.append("reference to view with name '").append(this.viewName).append("'");
		}
		else {
			buf.append("materialized View is [").append(this.view).append(']');
		}
		buf.append("; model is ").append(this.model);
		return buf.toString();
	}

}
