/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */


package org.springframework.context;

import java.util.EventListener;

/**
 * Interface to be implemented by event listeners.
 * Based on standard java.util base class for Observer
 * design pattern.
 * @author  Rod Johnson
 */
public interface ApplicationListener extends EventListener {

	/**
	* Handle an application event
	* @param e event to respond to
	*/
    void onApplicationEvent(ApplicationEvent e);

}

