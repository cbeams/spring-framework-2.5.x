/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context;

import java.util.EventObject;

/**
 * Class to be extended by all application events.
 * Abstract as it doesn't make sense for generic events
 * to be published directly.
 * @author Rod Johnson
 */
public abstract class ApplicationEvent extends EventObject {

	/** System time when the event happened */
	private long timestamp;

	/**
	 * Creates a new ApplicationEvent.
	 * @param source component that published the event
	 */
	public ApplicationEvent(Object source) {
		super(source);
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Return the system time in milliseconds when the event happened.
	 * @return the system time in milliseconds when the event happened
	 */
	public long getTimestamp() {
		return timestamp;
	}

}
