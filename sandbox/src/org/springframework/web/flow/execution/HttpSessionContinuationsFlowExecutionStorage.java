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
package org.springframework.web.flow.execution;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.util.WebUtils;

/**
 * Flow execution storage that stores flow executions as <i>continuations</i>
 * in the HttpSession.
 * <p>
 * This storage strategy requires a <code>HttpServletRequestEvent</code>.
 * 	
 * @see org.springframework.web.flow.execution.HttpServletRequestEvent
 * 
 * @author Erwin Vervaet
 */
public class HttpSessionContinuationsFlowExecutionStorage extends HttpSessionFlowExecutionStorage {

	public FlowExecution load(Event requestingEvent, String uniqueId)
			throws NoSuchFlowExecutionException, FlowExecutionStorageException {
		try {
			FlowExecutionContinuation continuation = 
				(FlowExecutionContinuation)
					WebUtils.getRequiredSessionAttribute(getHttpServletRequest(requestingEvent), uniqueId);
			return continuation.getFlowExecution();
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(uniqueId, e);
		}
	}

	public String save(Event requestingEvent, String uniqueId, FlowExecution flowExecution)
			throws FlowExecutionStorageException {
		// generate a new id for each continuation!
		uniqueId = generateUniqueId();
		getHttpSession(requestingEvent).setAttribute(uniqueId, new FlowExecutionContinuation(flowExecution));
		return uniqueId;
	}

	public void remove(Event requestingEvent, String uniqueId) throws FlowExecutionStorageException {
		getHttpSession(requestingEvent).removeAttribute(uniqueId);
	}

}
