/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

/**
 * This is implemented by classes that want to have lifecycle methods
 * called after all other spring initialization has taken place.  This is typically
 * needed for loosely coupled services that are not used inside a container
 * such as JMS and JMX.  The existing spring initialization facilities
 * do not guaranteee the order in which all beans get created, created and initialized.
 * This is a problems if a a JMS Service starts to consume messages before other
 * loosely couples classes are fully initialized.
 *  
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public interface ServiceBean
{


    /**
     * Called to initialize the bean, after the BeanFactory creates and 
     * potentially initializes all the beans.  
     * @throws Exception if there is a problem during initialization.  
     *
     */
    void initialize() throws Exception;
    
    
    /**
     * Called to start the bean after it has been initialized.
     *
     */
    void start();
    
    /**
     * Called to dispose of resources held by the bean, 
     * typically after it had been stoped. 
     *
     */
    void dispose();
    
    /**
     * Called to stop the bean, typically after it had been started.
     *
     */
    void stop();
}
