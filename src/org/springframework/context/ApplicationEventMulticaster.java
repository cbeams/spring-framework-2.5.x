/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
 package org.springframework.context;

/**
 * Subinterface of ApplicationListener to be implemented by
 * listeners that can broadcast events to other listeners.
 * @author Rod Johnson
 */
public interface ApplicationEventMulticaster extends ApplicationListener {

	/**
	 * Add a listener to be notified of all events
	 * @param listener listener to add
	 */
	void addApplicationListener(ApplicationListener listener);

	/**
	 * Remove a listener in the notification list]
	 * @param listener listener to remove
	 */
	void removeApplicationListener(ApplicationListener listener);

	/**
	 * Remove all listeners registered with this multicaster.
	 * It will perform no action on event notification until more
	 * listeners are registered.
	 */
	void removeAllListeners();

}

