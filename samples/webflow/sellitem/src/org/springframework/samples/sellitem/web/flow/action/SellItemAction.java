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
package org.springframework.samples.sellitem.web.flow.action;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.action.FormAction;

public class SellItemAction extends FormAction {

	public SellItemAction() {
		setFormObjectName("sale");
		setFormObjectClass(Sale.class);
		setFormObjectScope(ScopeType.FLOW);
	}

	protected boolean suppressValidation(RequestContext context) {
		return !getActionStateAction(context).containsProperty(VALIDATOR_METHOD_PROPERTY);
	}

	public Event isShipping(RequestContext context) throws Exception {
		//TODO: improve this with conditional transitions
		Sale sale = (Sale)context.getFlowScope().getAttribute("sale");
		return sale.isShipping() ? result("yes") : result("no");
	}
}