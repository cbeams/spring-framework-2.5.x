/*
 * Copyright 2002-2004 the original author or authors.
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
	private final long timestamp;

	/**
	 * Create a new ApplicationEvent.
	 * @param source the component that published the event
	 */
	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Return the system time in milliseconds when the event happened.
	 */
	public long getTimestamp() {
		return timestamp;
	}

}
