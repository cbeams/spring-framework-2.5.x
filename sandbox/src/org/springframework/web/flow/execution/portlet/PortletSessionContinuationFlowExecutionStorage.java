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
package org.springframework.web.flow.execution.portlet;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.execution.FlowExecutionContinuation;
import org.springframework.web.flow.execution.FlowExecutionStorageException;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * Flow execution storage that stores flow executions as <i>continuations</i>
 * in the PortletSession.
 * <p>
 * A downside of this storage strategy (and of server-side continuations in
 * general) is that there could be many copies of the flow execution stored in
 * the portlet session, increasing server memory requirements.
 * <p>
 * This storage strategy requires a <code>PortletRequestEvent</code>.
 * 
 * @see org.springframework.web.flow.execution.portlet.PortletRequestEvent
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 */
public class PortletSessionContinuationFlowExecutionStorage extends PortletSessionFlowExecutionStorage {

	private boolean compress = false;

	/**
	 * Returns whether or not continuations should be compressed.
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * Set whether or not continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecution load(String id, Event requestingEvent)
			throws NoSuchFlowExecutionException, FlowExecutionStorageException {
		try {
			FlowExecutionContinuation continuation =
				(FlowExecutionContinuation)PortletUtils.getRequiredSessionAttribute(getPortletRequest(requestingEvent), id);
			return continuation.getFlowExecution();
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(id, e);
		}
	}

	public String save(String id, FlowExecution flowExecution, Event requestingEvent)
			throws FlowExecutionStorageException {
		// generate a new id for each continuation
		id = createId();
		getPortletSession(requestingEvent).setAttribute(id,
				new FlowExecutionContinuation(flowExecution, isCompress()));
		return id;
	}

	public void remove(String id, Event requestingEvent) throws FlowExecutionStorageException {
		// nothing to do
		// note that we shouldn't remove the identified flow execution continuation
		// because that id actually identifies the 'previous' flow execution, not the
		// one that has ended (because that one is never saved so doesn't even have
		// an id!)
	}
}