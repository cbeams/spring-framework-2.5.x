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
package org.springframework.web.flow.struts;

import org.apache.struts.action.ActionMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.execution.ClientContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.FlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionFlowExecutionStorage;

/**
 * A flow action mapping object that allows FlowActions to be configured with a
 * flowId and storage strategy via a Struts <code>&lt;set-property/&gt;</code>
 * definition.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowActionMapping extends ActionMapping {
	
	// known flow execution storage strategies
	
	/**
	 * Constant indicating use of the default flow execution storage strategy:
	 * {@link HttpSessionFlowExecutionStorage}.
	 */
	public static final String STORAGE_DEFAULT = "default";
	
	/**
	 * Constant indicating use of the HTTP session flow execution storage
	 * strategy: {@link HttpSessionFlowExecutionStorage}.
	 */
	public static final String STORAGE_SESSION = "session";
	
	/**
	 * Constant indicating use of the HTTP session continuations based flow
	 * execution storage strategy: {@link HttpSessionContinuationFlowExecutionStorage}.
	 */
	public static final String STORAGE_SESSION_CONTINUATION = "sessionContinuation";
	
	/**
	 * Constant indicating use of the client side continuation based flow
	 * execution storage strategy: {@link ClientContinuationFlowExecutionStorage}.
	 */
	public static final String STORAGE_CLIENT_CONTINUATION = "clientContinuation";
	
	/**
	 * The id of the flow whose executions the flow action should manage.
	 */
	private String flowId;

	/**
	 * The encoded flow storage strategy to use -- supported values:
	 * ("default", "session", "sessionContinuation", "clientContinuation")
	 */
	private String storage = STORAGE_DEFAULT;
	
	/**
	 * Returns the flowId.
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Set the flowId.
	 */
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	/**
	 * Returns the encoded flow execution storage strategy.
	 * @return the encoded storage strategy
	 */
	public String getStorage() {
		return storage;
	}

	/**
	 * Sets the encoded flow execution storage strategy.
	 * @param storage the storage strategy
	 */
	public void setStorage(String storage) {
		this.storage = storage;
	}
	
	/**
	 * Returns the storage strategy encoded in the action mapping.
	 * @throws IllegalArgumentException when the encoded storage strategy is not
	 *         recognized
	 */
	public FlowExecutionStorage getFlowExecutionStorage() throws IllegalArgumentException {
		if (!StringUtils.hasText(storage) || storage.equalsIgnoreCase(STORAGE_DEFAULT)
				|| storage.equalsIgnoreCase(STORAGE_SESSION)) {
			return new HttpSessionFlowExecutionStorage();
		}
		else if (storage.equalsIgnoreCase(STORAGE_SESSION_CONTINUATION)) {
			return new HttpSessionContinuationFlowExecutionStorage();
		}
		else if (storage.equalsIgnoreCase(STORAGE_CLIENT_CONTINUATION)) {
			return new ClientContinuationFlowExecutionStorage();
		}
		else {
			throw new IllegalArgumentException("Unknown flow execution storage type: '" + storage + "'");
		}
	}
}