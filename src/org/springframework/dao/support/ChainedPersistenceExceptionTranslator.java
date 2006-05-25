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

package org.springframework.dao.support;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * Implementation of PersistenceExceptionTranslator that
 * supports chaining, allowing the addition of PersistenceExceptionTranslator
 * instances in order. Returns non-null on the first (if any)
 * match.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class ChainedPersistenceExceptionTranslator implements PersistenceExceptionTranslator {
	
	/**
	 * List<PersistenceExceptionTranslator>
	 */
	private List translators = new LinkedList();
	
	public void add(PersistenceExceptionTranslator pet) {
		translators.add(pet);
	}

	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		DataAccessException translatedDex = null;
		for (Iterator it = translators.iterator(); translatedDex == null && it.hasNext(); ) {
			PersistenceExceptionTranslator pet = (PersistenceExceptionTranslator) it.next();
			translatedDex = pet.translateExceptionIfPossible(ex);
		}
		return translatedDex;
	}

}
