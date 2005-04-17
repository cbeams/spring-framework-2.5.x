/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.samples.flowlauncher;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;

public class SampleFlowExecutionListener extends FlowExecutionListenerAdapter {
	
	public static final String INPUT_ATTRIBUTE = "input";

	public void requestSubmitted(RequestContext context) {
		/*
		 * On each request coming into the flow, check if there is input data in the
		 * request and if so, put it in flow scope.
		 * You could also do this in a "captureInput" action, but using a flow execution
		 * listener is more flexible.
		 */
		String input = (String)context.getOriginatingEvent().getParameter(INPUT_ATTRIBUTE);
		if (StringUtils.hasText(input)) {
			//put the input in the flow scope
			context.getFlowScope().setAttribute(INPUT_ATTRIBUTE, input);
		}
		context.getFlowScope().assertAttributePresent(INPUT_ATTRIBUTE);
	}

}
