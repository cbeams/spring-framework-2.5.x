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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Abstract implementation of the {@link BindingResult} interface, assuming
 * that the BindingResult object serves as adapter for pre-resolved errors
 * and hence does not allow for registration of new errors.
 *
 * @author Juergen Hoeller
 * @since 2.5.3
 */
public abstract class AbstractFrozenBindingResult extends AbstractFrozenErrors implements BindingResult {

	public Map getModel() {
		Map model = new HashMap(2);
		model.put(MODEL_KEY_PREFIX + getObjectName(), this);
		model.put(getObjectName(), getTarget());
		return model;
	}

	public PropertyEditorRegistry getPropertyEditorRegistry() {
		return null;
	}

	public void addError(ObjectError error) {
		throw new UnsupportedOperationException("Error addition not supported - BindingResult is frozen");
	}

	public String[] resolveMessageCodes(String errorCode, String field) {
		throw new UnsupportedOperationException("Error addition not supported - BindingResult is frozen");
	}

	public void recordSuppressedField(String field) {
		throw new UnsupportedOperationException("Suppressed field recording not supported - BindingResult is frozen");
	}

	public String[] getSuppressedFields() {
		return new String[0];
	}

}
