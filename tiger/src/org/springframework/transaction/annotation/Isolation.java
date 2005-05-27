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
 * Enumeration that represents transaction isolation levels
 * for use with the JDK 1.5+ transaction annotation, corresponding
 * to the TransactionDefinition interface.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.TransactionDefinition
 */
public enum Isolation {
	
	DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),
	READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),
	READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),
	REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),
	SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);

	private final int value;

	Isolation(int value) { this.value = value; }
	
	public int value() { return value; }
	
}
