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

package org.springframework.transaction;

/**
 * Exception that represents a transaction failure caused by heuristics.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 17-Mar-2003
 */
public class HeuristicCompletionException extends TransactionException {

	/**
	 * Values for the outcome state of a heuristically completed transaction.
	 */
	public static final int STATE_UNKNOWN = 0;
	public static final int STATE_COMMITTED = 1;
	public static final int STATE_ROLLED_BACK = 2;
	public static final int STATE_MIXED = 3;

	/**
	 * The outcome state of the transaction: have some or all resources been committed?
	 */
	private int outcomeState = STATE_UNKNOWN;

	public static String getStateString(int state) {
		switch (state) {
			case STATE_COMMITTED:
				return "committed";
			case STATE_ROLLED_BACK:
				return "rolled back";
			case STATE_MIXED:
				return "mixed";
			default:
				return "unknown";
		}
	}

	public HeuristicCompletionException(int outcomeState, Throwable ex) {
		super("Heuristic completion: outcome state is " + getStateString(outcomeState), ex);
		this.outcomeState = outcomeState;
	}

	public int getOutcomeState() {
		return outcomeState;
	}

}
