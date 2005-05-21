/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.config;

import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.TransitionCriteriaFactory;

/**
 * Simple default implementation of the transition criteria factory.
 * It creates a default constraint implementation that will match true
 * on events with the provided event id. If the given event id is "*",
 * a wildcard event criteria object will be returned that matches any event.
 * Otherwise you get a criteria object that matches given event id exactly.
 *
 * @author Rob Harrop
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class SimpleTransitionCriteriaCreator implements TransitionCriteriaCreator {

	public TransitionCriteria create(String encodedCriteria) {
		return createDefaultTransitionCriteria(encodedCriteria);
	}

	/**
	 * Create a default constraint implementation that will match true on events
	 * with the provided event id.
	 * <p>
	 * If the given event id is "*", a wildcard event criteria object will be
	 * returned that matches any event. Otherwise you get a criteria object that
	 * matches given event id exactly.
	 */
	protected TransitionCriteria createDefaultTransitionCriteria(String encodedCriteria) {
		if (TransitionCriteriaFactory.WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return TransitionCriteriaFactory.any();
		}
		else {
			return TransitionCriteriaFactory.eventId(encodedCriteria);
		}
	}
}
