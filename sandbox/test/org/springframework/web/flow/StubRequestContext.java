/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.web.flow;

import java.util.Map;

/**
 * Stub implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * TODO - finish
 * @author Keith Donald
 */
public class StubRequestContext implements RequestContext {

	public Flow getActiveFlow() throws IllegalStateException {
		return null;
	}

	public State getCurrentState() throws IllegalStateException {
		return null;
	}

	public FlowExecutionListenerList getFlowExecutionListenerList() {
		return null;
	}

	public Scope getFlowScope() {
		return null;
	}

	public Event getLastEvent() {
		return null;
	}

	public Map getModel() {
		return null;
	}

	public Event getOriginatingEvent() {
		return null;
	}

	public Scope getRequestScope() {
		return null;
	}

	public Flow getRootFlow() {
		return null;
	}

	public TransactionSynchronizer getTransactionSynchronizer() {
		return null;
	}

	public boolean isFlowExecutionActive() {
		return false;
	}

	public boolean isRootFlowActive() {
		return false;
	}
}