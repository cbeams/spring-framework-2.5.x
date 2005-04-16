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
package org.springframework.samples.flowlauncher.web.flow.action;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.MultiAction;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

public class SampleAction extends MultiAction {
	
	public static final String INPUT_ATTRIBUTE = "input";

	/**
	 * Setup input attributes for the sample flows.
	 */
	public Event captureInput(RequestContext context) throws Exception {
		//check to see if input was explicitly specified in the request
		HttpServletRequest request = ((HttpServletRequestEvent)context.getOriginatingEvent()).getRequest();
		String input = request.getParameter(INPUT_ATTRIBUTE);
		if (StringUtils.hasText(input)) {
			//put the input in the flow scope
			context.getFlowScope().setAttribute(INPUT_ATTRIBUTE, input);
		}
		if (context.getFlowScope().containsAttribute(INPUT_ATTRIBUTE)) {
			return success();
		}
		else {
			throw new IllegalStateException("input parameter cannot be found");
		}
	}
}
