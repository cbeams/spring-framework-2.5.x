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
package org.springframework.web.flow.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * Utility class providing convenience methods for the Flow system.
 * 
 * This class provides web transaction token handling methods similar to those
 * available in the Struts framework. In essense an implementation of the <a
 * href="http://www.javajunkies.org/index.pl?lastnode_id=3361&node_id=3355">synchronizer
 * token </a> pattern. You can use this to prevent double submits in the
 * following way:
 * <ul>
 * <li>Create an action that will mark the beginning of the transactional part
 * of your flow. In this action you do
 * <code>FlowUtils.saveToken(model, "token")</code> to put a unique
 * transaction token in the flow model.</li>
 * <li>On a page inside the transactional part of the flow, add the token to
 * the request that will be send to the controller. When you're using an HTML
 * form, you can use a hidden field to do this:
 * <code>&lt;INPUT type="hidden" name="_token" value="&lt;%=request.getAttribute("token") %&gt;"&gt;</code>
 * </li>
 * <li>Finally, check the token using
 * <code>FlowUtils.isTokenValid(model, "token", request, "_token", true)</code>
 * in the action that processes the transactional data. If the token is valid
 * you do real processing, otherwise you return some event to indicate an
 * alternative outcome (e.g. an error page).</li>
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowUtils {

	/**
	 * Retrieve information about the current flow execution.
	 * @param model The model for the executing flow.
	 * @return The flow execution
	 */
	public static FlowExecution getFlowExecution(AttributesAccessor model) {
		return (FlowExecution)model.getRequiredAttribute(FlowExecution.ATTRIBUTE_NAME);
	}

	// token related functionality like in Struts

	/**
	 * <p>
	 * Save a new transaction token in given model.
	 * 
	 * @param model the model map where the generated token should be saved
	 * @param tokenName the key used to save the token in the model map
	 */
	public static void setToken(MutableAttributesAccessor model, String tokenName) {
		String txToken = new RandomGuid().toString();
		synchronized (model) {
			model.setAttribute(tokenName, txToken);
		}
	}

	/**
	 * Reset the saved transaction token in given model. This indicates that
	 * transactional token checking will not be needed on the next request that
	 * is submitted.
	 * 
	 * @param model the model map where the generated token should be saved
	 * @param tokenName the key used to save the token in the model map
	 */
	public static void clearToken(MutableAttributesAccessor model, String tokenName) {
		synchronized (model) {
			model.removeAttribute(tokenName);
		}
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in
	 * given model, and the value submitted as a request parameter with this
	 * action matches it. Returns <code>false</code> when
	 * <ul>
	 * <li>there is no transaction token saved in the model</li>
	 * <li>there is no transaction token included as a request parameter</li>
	 * <li>the included transaction token value does not match the transaction
	 * token in the model</li>
	 * </ul>
	 * 
	 * @param model the model map where the token is stored
	 * @param tokenName the key used to save the token in the model map
	 * @param request current HTTP request
	 * @param requestParameterName name of the request parameter holding the
	 *        token
	 * @param reset indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	public static boolean isTokenValid(MutableAttributesAccessor model, HttpServletRequest request, String tokenName,
			String requestParameterName, boolean clear) {
		String tokenValue = request.getParameter(requestParameterName);
		return isTokenValid(model, tokenName, tokenValue, clear);
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in
	 * given model and the given value matches it. Returns <code>false</code>
	 * when
	 * <ul>
	 * <li>there is no transaction token saved in the model</li>
	 * <li>given token value is empty</li>
	 * <li>the given transaction token value does not match the transaction
	 * token in the model</li>
	 * </ul>
	 * 
	 * @param model the model map where the token is stored
	 * @param tokenName the key used to save the token in the model map
	 * @param tokenValue the token value to check
	 * @param clear indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	public static boolean isTokenValid(MutableAttributesAccessor model, String tokenName, String tokenValue,
			boolean clear) {
		if (!StringUtils.hasText(tokenValue)) {
			return false;
		}
		synchronized (model) {
			String txToken = (String)model.getAttribute(tokenName);
			if (!StringUtils.hasText(txToken)) {
				return false;
			}
			if (clear) {
				clearToken(model, tokenName);
			}
			return txToken.equals(tokenValue);
		}
	}
}