/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * by a DispatcherServlet. The View can take the form of a reference
 * to a View object, or a String view name which will need to be
 * resolved by a ViewResolver object. The model is a Map, allowing
 * the use of multiple data objects keyed by name.
 *
 * @author Rod Johnson
 * @see ViewResolver
 */
public class ModelAndView {

	/** Model */
	private Map model;

	/** View if we hold an object reference */
	private View view;

	/** 
	 * View name if we hold a view name that will be resolved
	 * by the DispatcherServlet via a ViewResolver
	 */
	private String viewName;

	/**
	 * Convenient constructor when there is no model data to expose.
	 * @param view view reference
	 */
	public ModelAndView(View view) {
		this.view = view;
		this.model = new HashMap();
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * @param viewName view name, resolved by the DispatcherServlet
	 */
	public ModelAndView(String viewName) {
		this.viewName = viewName;
		this.model = new HashMap();
	}

	/**
	 * Creates new ModelAndView given a View object and a model.
	 * @param view view to render this model
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be null, but the model Map
	 * may be null if there is no model data.
	 */
	public ModelAndView(View view, Map model) {
		this.view = view;
		this.model = model;
	}

	/** 
	 * Creates new ModelAndView given a view name and a model.
	 * @param viewName name of the View to render this model.
	 * This will be resolved by the DispatcherServlet at runtime.
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be null, but the model Map
	 * may be null if there is no model data.
	 */
	public ModelAndView(String viewName, Map model) {
		this.viewName = viewName;
		this.model = model;
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param view view reference
	 * @param modelName name of the single entry in the model
	 * @param modelObject modelObject the single model object
	 */
	public ModelAndView(View view, String modelName, Object modelObject) {
		this(view);
		this.model.put(modelName, modelObject);
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the view
	 * @param modelName name of the single entry in the model
	 * @param modelObject modelObject the single model object
	 */
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this(viewName);
		this.model.put(modelName, modelObject);
	}

	/**
	 * Add an object to the model.
	 * @param name name of the object to add to the model
	 * @param object object to add to the model. May not be null.
	 * @return this object, convenient to allow usages like
	 * return modelAndView.addObject("foo", bar);
	 */
	public ModelAndView addObject(String name, Object object) {
		this.model.put(name, object);
		return this;
	}

	/**
	 * Return whether we use a view reference, i.e. true if the
	 * view has been specified via a name to be resolved by the
	 * DispatcherServlet via a ViewResolver.
	 */
	public boolean isReference() {
		return viewName != null;
	}

	/**
	 * Return the View object, or null if we are using a view name
	 * to be resolved by the DispatcherServlet via a ViewResolver.
	 */
	public View getView() {
		return view;
	}

	/**
	 * Return the view name to be resolved by the DispatcherServlet
	 * via a ViewResolver, or null if we are using a View object.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Return the model map. May be null.
	 */
	public Map getModel() {
		return model;
	}

	/**
	 * Return diagnostic information about this model and view.
	 */
	public String toString() {
		String s = "ModelAndView: ";
		s += isReference() ? "reference to view with name [" + viewName + "]" :
				"materialized View is [" + view + "]";
		s += "; Model=[" + model + "]";
		return s;
	}

}
