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

import org.springframework.util.enums.support.ShortCodedLabeledEnum;

/**
 * Supported scope types for the web flow system.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ScopeType extends ShortCodedLabeledEnum {

	/**
	 * Constant indicating request scope. The request scope is local to
	 * a request being executed by a flow execution.
	 */
	public static final ScopeType REQUEST = new ScopeType(0, "request");

	/**
	 * Constant indicating flow scope. The flow scope is shared by all
	 * artifacts of a flow: actions, view, states, ...
	 */
	public static final ScopeType FLOW = new ScopeType(1, "flow");

	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private ScopeType(int code, String label) {
		super(code, label);
	}
}