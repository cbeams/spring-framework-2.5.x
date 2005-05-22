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

package org.springframework.samples.jca.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.samples.jca.model.Person;
import org.springframework.samples.jca.service.PersonService;

/**
 * Standalone sample that uses a jdbc connector to get informations
 * about a person.
 *
 * Accesses applicationContext.xml file from the classpath
 * of the application.
 *
 * @author Thierry TEMPLIER
 * @since 27/12/2004
 */
public class StandaloneJca {

	private final PersonService service;

	private static final Log log = LogFactory.getLog(StandaloneJca.class);

	public StandaloneJca(PersonService service) {
		this.service = service;
	}

	public void execute(int id) {
		try {
			Person person=service.getPerson(id);
			log.debug("previous lastname = "+person.getLastName());
			log.debug("previous firstname = "+person.getFirstName());

			person.setFirstName("Peter");
			person.setLastName("Black");
			service.updatePerson(person);

			person=service.getPerson(id);
			log.debug("new lastname = "+person.getLastName());
			log.debug("new firstname = "+person.getFirstName());
		} catch(Exception ex) {
			log.error("Error during the execution",ex);
		}
	}

	public static void main(String[] args) {// throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/applicationContext.xml");
		PersonService service = (PersonService) context.getBean("personService");
		StandaloneJca tool = new StandaloneJca(service);
		tool.execute(1);
	}

}
