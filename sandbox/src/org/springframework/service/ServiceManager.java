/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContextAware;

/**
 * The container for putting the ServiceBeans through their lifecycle.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public interface ServiceManager extends ApplicationContextAware, DisposableBean, ServiceBean
{

    /**
     * Validate that all the managed service beans are singletons
     * @return true if all registered ServiceBean instances are singletons.
     *
     */
    boolean validateSingleton();
    
    
   
}
