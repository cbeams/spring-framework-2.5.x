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
package org.springframework.rules.closure;

/**
 * Convenient super class for closures that encapsulate a block of executable
 * code. Subclasses should override <code>Object call(Object)</code> for executing a block of code
 * with a return result. Subclasses should override <code>void handle(Object)</code> for
 * executing a block of code without a result.
 *
 * @author Keith Donald
 */
public abstract class Block extends AbstractClosure {

	public Object call(Object argument) {
		handle(argument);
		return NULL_VALUE;
	}

	protected void handle(Object argument) {
		throw new UnsupportedOperationException(
				"You must override call(arg) for processing an arg with a return value, "
				+ "or handle(arg) for processing a single argument with no return result.");
	}

}