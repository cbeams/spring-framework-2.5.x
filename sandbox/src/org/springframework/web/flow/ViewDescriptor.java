/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ToStringCreator;

/**
 * <p>Holder class for a Model and View in the web flow framework.</p>
 * 
 * <p>Similar to Spring MVC's ModelAndView.</p>
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ViewDescriptor {

    public static final ViewDescriptor NULL_OBJECT = new ViewDescriptor() {
        public String toString() {
            return "[" + ClassUtils.getShortName(ViewDescriptor.class) + " [null] object]";
        }
    };

    private Map model = new HashMap();

    private String viewName;

    private ViewDescriptor() {
    }

    /**
     * Creates a ViewDescriptor with the specified logical View name, and an empty Model.
     * @param viewName the view name
     */
    public ViewDescriptor(String viewName) {
        Assert.hasText(viewName, "The view name is required");
        this.viewName = viewName;
    }

    /**
     * Created a ViewDescriptor with the specified logical View name and Model
     * @param viewName the view name
     * @param attributes the model
     */
    public ViewDescriptor(String viewName, Map attributes) {
        Assert.hasText(viewName, "The view name is required");
        this.viewName = viewName;
        this.model = new HashMap(attributes);
    }

    /**
     * Created a ViewDescriptor with the specified logical View name and a Model
     * containing one attribute
     * @param viewName the view name
     * @param attributeName the name of the attribute to put in the Model
     * @param attributeValue the value of the attribute to put in the Model
     */
    public ViewDescriptor(String viewName, String attributeName, Object attributeValue) {
        Assert.hasText(viewName, "The view name is required");
        this.viewName = viewName;
        setAttribute(attributeName, attributeValue);
    }

    /**
     * Adds or replaces an attribute in the model
     * @param attributeName the name of the attribute to put in the Model
     * @param attributeValue the value of the attribute to put in the Model
     */
    public void setAttribute(String attributeName, Object attributeValue) {
        model.put(attributeName, attributeName);
    }

    /**
     * Adds or replaces a number of attributes in the model
     * @param attributes a source Map containing attributes to add or replace in the model
     */
    public void setAll(Map attributes) {
        this.model.putAll(attributes);
    }

    /**
     * @return the logical View name
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * @return the model
     */
    public Map getModel() {
        return Collections.unmodifiableMap(model);
    }

    public String toString() {
        return new ToStringCreator(this).append("viewName", viewName).toString();
    }
}
