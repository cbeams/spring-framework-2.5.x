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
package org.springframework.web.flow.struts;

import org.apache.struts.action.ActionMapping;

/**
 * A flow action mapping object that allows FlowActions to be configured with a flowId
 * via a Struts <code>&lt;set-property/&gt;</code> definition.
 * 
 * @author Keith Donald
 */
public class FlowActionMapping extends ActionMapping {
	
	private String flowId;

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
}