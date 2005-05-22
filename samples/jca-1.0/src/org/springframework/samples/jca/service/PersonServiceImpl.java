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

import org.springframework.samples.jca.dao.PersonDao;
import org.springframework.samples.jca.exception.PersonException;
import org.springframework.samples.jca.model.Person;

/**
 * @author Thierry TEMPLIER
 */
public class PersonServiceImpl implements PersonService {
	private PersonDao personDao;

	/**
	 * @see org.springframework.samples.jca.service.PersonService#getPerson(int)
	 */
	public Person getPerson(int id) throws PersonException {
		return personDao.getPerson(id);
	}

	/**
	 * @see org.springframework.samples.jca.service.PersonService#updatePerson(org.springframework.samples.jca.model.Person)
	 */
	public void updatePerson(Person person) {
		personDao.updatePerson(person);
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	public void setPersonDao(PersonDao dao) {
		personDao = dao;
	}

}
