/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.validation.support;

import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;

/**
 * Abstract implementation of the {@link Errors} interface, assuming that
 * the Errors object serves as adapter for pre-resolved errors and hence
 * does not allow for registration of new errors.
 *
 * @author Juergen Hoeller
 * @since 2.5.3
 */
public abstract class AbstractFrozenErrors extends AbstractErrors {

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		throw new UnsupportedOperationException("Rejection not supported - Errors object is frozen");
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		throw new UnsupportedOperationException("Rejection not supported - Errors object is frozen");
	}

	public void addAllErrors(Errors errors) {
		throw new UnsupportedOperationException("Rejection not supported - Errors object is frozen");
	}

}
