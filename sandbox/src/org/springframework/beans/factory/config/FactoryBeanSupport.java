/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.FactoryBean;

/**
 * A simple convenince, template superclass for factory bean implementations.
 * <p>
 * <p>
 * Tracks whether or not the FactoryBean should be singleton. If it is a
 * singleton, this class will create once and subsequently return the singleton
 * instance. If it is a prototype, this class will create a new instance each
 * time. Subclasses are responsible for implementing the abstract
 * <code>create</code> template method which should encapsulate bean creation
 * logic.
 * 
 * @author Keith Donald
 */
public abstract class FactoryBeanSupport implements FactoryBean {
    private boolean singleton;
    private Object singleInstance;

    /**
     * Constructs a factory bean support that treats the Bean created by this
     * factory as a singleton.
     */
    public FactoryBeanSupport() {
        setSingleton(singleton);
    }

    /**
     * Creates a factory bean support template either creating a singleton or
     * prototypes.
     * 
     * @param singleton
     *            Is the bean created by this factory a singleton?
     */
    public FactoryBeanSupport(boolean singleton) {
        setSingleton(singleton);
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        if (isSingleton()) {
            return singleInstance();
        } else {
            return create();
        }
    }

    /**
     * Configure singleton status of beans created by this factory.
     * 
     * @param singleton
     *            Is the bean created by this factory a singleton?
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    private Object singleInstance() throws Exception {
        if (singleInstance == null) {
            this.singleInstance = create();
        }
        return singleInstance;
    }

    /**
     * Subclasses must override to return the type (class or interface) of
     * object produced by this factory.
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public abstract Class getObjectType();

    /**
     * Template method subclasses must override to construct the object returned
     * by this factory.
     * 
     * @return The object produced by this factory (a singleton or prototype)
     * @throws Exception
     *             if an exception occured during creation
     */
    protected abstract Object create() throws Exception;

}