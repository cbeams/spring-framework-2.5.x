/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.springframework.context.ApplicationContextAware;

/**
 * Suggested methods for a stand-alone application.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public interface Application extends ApplicationContextAware
{

    /**
     * Get the name of the application
     * @return the name of the application
     */
    String getName();

    /**
     * Set the name of the application.
     * @param name the name of the application
     */
    void setName(String name);

    /**
     * Get the version of the application.
     * @return the version of the application.
     */
    String getVersion();

    /**
     * Set the version of the application.
     * @param version the version of the application
     */
    void setVersion(String version);

    /**
     * Get the description of the server.
     * @return the descrition of the server.
     */
    String getDescription();

    /**
     * Set the description of the application.
     * @param desc the description of the application.
     */
    void setDescription(String desc);
    
    /**
     * Set the threads that will be registered with Runtime.getRuntime().addShutdownHook(Thread t)
     * @param threads The threads that will be registered.
     */
    //void setShutdownHookThreads(Thread[] threads);
    
    /**
     * Classes that implement ShutdownHook interface have their shutdown method called
     * in the order of registration on a single shutdown thread.
     * @param hooks
     */
   // void setShutdownHooks(ShutdownHook[] hooks);
    
    /**
     * Classes that do not implement the ShutdownHook interface can specify a method,object
     * key-value pair to be called on shutdown.  The method must return void and contain
     * zero args. If it name to
     * be called on the 
     * and the object 
     * a method name
     * for a zero parameter method that returns void.  
     * method name to be called on an object when 
     * @param methodObjectMap
     */
   // void setShutdownMethods(Map methodObjectMap);
    
}
