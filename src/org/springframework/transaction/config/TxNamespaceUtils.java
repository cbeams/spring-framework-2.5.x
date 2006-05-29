/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.transaction.config;

import org.springframework.core.Conventions;
import org.springframework.util.ClassUtils;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class TxNamespaceUtils {

	public static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	public static final String TRANSACTION_MANAGER_PROPERTY =
			Conventions.attributeNameToPropertyName(TRANSACTION_MANAGER_ATTRIBUTE);

	public static final String TRANSACTION_ATTRIBUTE_SOURCE = "transactionAttributeSource";

	private static final String ANNOTATION_TRANSACTION_ATTRIBUTE_SOURCE_CLASS_NAME =
			"org.springframework.transaction.annotation.AnnotationTransactionAttributeSource";


	public static Class getAnnotationTransactionAttributeSourceClass() {
		try {
			return ClassUtils.forName(ANNOTATION_TRANSACTION_ATTRIBUTE_SOURCE_CLASS_NAME);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Unable to load AnnotationTransactionAttributeSource class. Are you running on Java 1.5+?");
		}
	}

}
