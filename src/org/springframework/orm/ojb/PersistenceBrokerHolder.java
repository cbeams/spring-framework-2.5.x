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

package org.springframework.orm.ojb;

import org.apache.ojb.broker.PersistenceBroker;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Holder wrapping an OJB PersistenceBroker.
 * PersistenceBrokerTransactionManager binds instances of this class
 * to the thread, for a given PBKey.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 */
public class PersistenceBrokerHolder extends ResourceHolderSupport {

	private final PersistenceBroker persistenceBroker;

	public PersistenceBrokerHolder(PersistenceBroker persistenceBroker) {
		this.persistenceBroker = persistenceBroker;
	}

	public PersistenceBroker getPersistenceBroker() {
		return persistenceBroker;
	}

}
