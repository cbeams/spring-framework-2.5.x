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
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public abstract class FlowNavigationException extends FlowException {

	private Flow flow;

	public FlowNavigationException(Flow flow) {
		super();
		setFlow(flow);
	}

	public FlowNavigationException(Flow flow, String message) {
		super(message);
		setFlow(flow);
	}

	public FlowNavigationException(Flow flow, String message, Throwable cause) {
		super(message, cause);
		setFlow(flow);
	}

	public FlowNavigationException(Flow flow, Throwable cause) {
		super(cause);
		setFlow(flow);
	}

	private void setFlow(Flow flow) {
		this.flow = flow;
	}

	protected Flow getFlow() {
		return flow;
	}
}