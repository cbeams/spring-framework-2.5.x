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

package org.springframework.samples.jca.service;

import org.springframework.samples.jca.exception.PersonException;
import org.springframework.samples.jca.model.Person;

/**
 * Service class to manage a person.
 * 
 * @author Thierry TEMPLIER
 */
public interface PersonService {
	/**
	 * Method used to get a person using its pk.
	 * 
	 * @param id the pk of the person
	 * @return the person
	 * @throws PersonException
	 */
	public Person getPerson(int id) throws PersonException;

	/**
	 * Method used to update a person
	 * @param person the person to update
	 */
	public void updatePerson(Person person);
}
