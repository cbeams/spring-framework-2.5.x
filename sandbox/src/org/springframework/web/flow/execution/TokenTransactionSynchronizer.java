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
package org.springframework.web.flow.execution;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.Scope;

/**
 * Application transaction synchronizer implementation that uses a
 * <i>synchronizer token</i> to implement application transaction functionality.
 * The token will be stored in flow scope for the duration of an application transaction.
 * This implies that there needs to be a unique flow execution for each running
 * application transaction.
 * 
 * @author Erwin Vervaet
 */
public class TokenTransactionSynchronizer implements TransactionSynchronizer {
	
	/**
	 * The transaction synchronizer token will be stored in the model using an
	 * attribute with this name ("txToken").
	 */
	public static final String TRANSACTION_TOKEN_ATTRIBUTE_NAME = "txToken";

	/**
	 * A client can send the transaction synchronizer token to a controller
	 * using a request parameter with this name ("_txToken").
	 */
	public static final String TRANSACTION_TOKEN_PARAMETER_NAME = "_txToken";
	
	
	private String transactionTokenAttributeName = TRANSACTION_TOKEN_ATTRIBUTE_NAME;
	
	private String transactionTokenParameterName = TRANSACTION_TOKEN_PARAMETER_NAME;
	
	private boolean secure = false;
	
	/**
	 * Get the name for the transaction token attribute. Defaults to "txToken".
	 */
	public String getTransactionTokenAttributeName() {
		return transactionTokenAttributeName;
	}
	
	/**
	 * Set the name for the transaction token attribute.
	 */
	public void setTransactionTokenAttributeName(String transactionTokenAttributeName) {
		this.transactionTokenAttributeName = transactionTokenAttributeName;
	}

	/**
	 * Get the name for the transaction token parameter in request events.
	 * Defaults to "_txToken".
	 */
	public String getTransactionTokenParameterName() {
		return transactionTokenParameterName;
	}
	
	/**
	 * Set the name for the transaction token parameter in request events.
	 */
	public void setTransactionTokenParameterName(String transactionTokenParameterName) {
		this.transactionTokenParameterName = transactionTokenParameterName;
	}

	/**
	 * Returns whether or not the transaction synchronizer tokens are
	 * cryptographically strong.
	 */
	public boolean isSecure() {
		return secure;
	}
	
	/**
	 * Set whether or not the transaction synchronizer tokens should be
	 * cryptographically strong.
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean inTransaction(RequestContext context, boolean end) {
		return isEventTokenValid(context.getFlowScope(), context.getSourceEvent(),
				getTransactionTokenAttributeName(), getTransactionTokenParameterName(), end);
	}
	
	public void assertInTransaction(RequestContext context, boolean end) throws IllegalStateException {
		Assert.state(inTransaction(context, end),
				"The request is not executing in the context of an application transaction");
	}
	
	public void beginTransaction(RequestContext context) {
		setToken(context.getFlowScope(), getTransactionTokenAttributeName());
	}
	
	public void endTransaction(RequestContext context) {
		clearToken(context.getFlowScope(), getTransactionTokenAttributeName());
	}

	/**
	 * Save a new transaction token in flow scope.
	 * @param flowScope the flow scope
	 * @param tokenName the key used to save the token in the scope
	 */
	protected void setToken(Scope flowScope, String tokenName) {
		String txToken = new RandomGuid(isSecure()).toString();
		flowScope.setAttribute(tokenName, txToken);
	}

	/**
	 * Reset the saved transaction token in the flow scope. This indicates that
	 * transactional token checking will not be needed on the next request event
	 * that is submitted.
	 * @param flowScope the flow scope
	 * @param tokenName the key used to save the token in the scope
	 */
	protected void clearToken(Scope flowScope, String tokenName) {
		flowScope.removeAttribute(tokenName);
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in the
	 * flow scope, and the value submitted as a event parameter matches it.
	 * Returns <code>false</code> when
	 * <ul>
	 * <li>there is no transaction token saved in the flow scope</li>
	 * <li>there is no transaction token included as an event parameter</li>
	 * <li>the included transaction token value does not match the transaction
	 * token in the flow scope</li>
	 * </ul>
	 * @param flowScope the flow scope
	 * @param event the request event containing the token to check
	 * @param tokenName the key used to save the token in the scope
	 * @param tokenParameterName name of the event parameter holding the token
	 * @param clear indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	protected boolean isEventTokenValid(Scope flowScope, Event event, String tokenName, String tokenParameterName,
			boolean clear) {
		// we use the originating event because we want to check that the
		// client request that came into the system has a transaction token
		String tokenValue = (String)event.getParameter(tokenParameterName);
		return isTokenValid(flowScope, tokenName, tokenValue, clear);
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in the
	 * flow scope and the given value matches it. Returns <code>false</code>
	 * when
	 * <ul>
	 * <li>there is no transaction token saved in the flow scope</li>
	 * <li>given token value is empty</li>
	 * <li>the given transaction token value does not match the transaction
	 * token in the flow scope</li>
	 * </ul>
	 * @param flowScope the flow scope
	 * @param tokenName the key used to save the token in the model
	 * @param tokenValue the token value to check
	 * @param clear indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	protected boolean isTokenValid(Scope flowScope, String tokenName, String tokenValue, boolean clear) {
		if (!StringUtils.hasText(tokenValue)) {
			return false;
		}
		String txToken = (String)flowScope.getAttribute(tokenName);
		if (!StringUtils.hasText(txToken)) {
			return false;
		}
		if (clear) {
			clearToken(flowScope, tokenName);
		}
		return txToken.equals(tokenValue);
	}

}
