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

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition; 

/**
 * Enumeration that represents transaction propagation behaviors
 * for use with the JDK 1.5+ transaction annotation, corresponding
 * to the TransactionDefinition interface.
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.TransactionDefinition
 */
public enum Propagation {
	
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
	NEVER(TransactionDefinition.PROPAGATION_NEVER),
	NESTED(TransactionDefinition.PROPAGATION_NESTED);

	private final int value;

	Propagation(int value) { this.value = value; }
	
	public int value() { return value; }
	
}
