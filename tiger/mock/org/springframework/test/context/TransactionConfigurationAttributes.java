/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context;

import java.io.Serializable;

import org.springframework.test.annotation.TransactionConfiguration;
import org.springframework.test.context.support.DefaultTransactionConfigurationAttributes;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <p>
 * Strategy interface for <em>transaction configuration attributes</em> for
 * test configuration.
 * </p>
 * <p>
 * Note: concrete implementations <strong>must</strong> implement sensible
 * {@link Object#equals(Object) equals()} and
 * {@link Object#hashCode() hashCode()} methods for caching purposes, etc. In
 * addition, concrete implementations <em>should</em> provide a sensible
 * {@link Object#toString() toString()} implementation.
 * </p>
 *
 * @see TransactionConfiguration
 * @see DefaultTransactionConfigurationAttributes
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public interface TransactionConfigurationAttributes extends Serializable {

	/**
	 * Gets the name of the {@link PlatformTransactionManager} that is to be
	 * used to drive transactions.
	 *
	 * @return The transaction manager name.
	 */
	public abstract String getTransactionManagerName();

	/**
	 * Whether or not transactions should be rolled back by default.
	 *
	 * @return The <em>default rollback</em> flag.
	 */
	public abstract boolean isDefaultRollback();

}
