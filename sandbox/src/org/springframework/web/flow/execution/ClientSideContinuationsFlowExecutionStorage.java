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

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.NoSuchFlowExecutionException;

/**
 * Flow execution storage implementation that will store a flow execution as a
 * <i>continuation</i> on the client side. It will actually encode the state of
 * the flow execution in the unique id that is returned from the
 * {@link #save(Event, String, FlowExecution) save} method. The load method
 * just decodes the incoming id and restores the <code>FlowExecution</code>
 * object.
 * 
 * @author Erwin Vervaet
 */
public class ClientSideContinuationsFlowExecutionStorage implements FlowExecutionStorage {

	public FlowExecution load(Event requestingEvent, String uniqueId)
			throws NoSuchFlowExecutionException, FlowExecutionStorageException {
		return decode(uniqueId);
	}

	public String save(Event requestingEvent, String uniqueId,
			FlowExecution flowExecution) throws FlowExecutionStorageException {
		return encode(flowExecution);
	}

	public void remove(Event requestingEvent, String uniqueId)
			throws FlowExecutionStorageException {
		// nothing to do here
	}
	
	/**
	 * Decode given data string, received from the client, and return the
	 * corresponding flow execution object.
	 */
	protected FlowExecution decode(String data) {
		return new FlowExecutionContinuation(Base64.decodeBase64(data.getBytes())).getFlowExecution();
	}
	
	/**
	 * Encode given flow execution object into a data string that can be
	 * stored on the client.
	 */
	protected String encode(FlowExecution flowExecution) {
		byte[] data = new FlowExecutionContinuation(flowExecution).getData();
		return new String(Base64.encodeBase64(data));
	}

}
