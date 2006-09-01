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
package org.springframework.orm.jpa.vendor;

import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.orm.jpa.EntityManagerPlusOperations;
import org.springframework.orm.jpa.EntityManagerPlusOperations1;

/**
 * @author Costin Leau
 * 
 */
public class HibernateJpaEntityManagerPlus implements EntityManagerPlusOperations, EntityManagerPlusOperations1 {

	private SessionFactoryImplementor sessionFactory;

	public HibernateJpaEntityManagerPlus(SessionFactory sessionFactory) {
		this.sessionFactory = (SessionFactoryImplementor) sessionFactory;
	}

	public Object getGeneratorByName(String generatorName) {
		return IdentifierGeneratorFactory.getIdentifierGeneratorClass(generatorName, sessionFactory.getDialect());
	}

	public Object getGeneratorForClass(Class mappedClass) {
		ClassMetadata metadata = sessionFactory.getClassMetadata(mappedClass);
		// do a sanity check
		if (!metadata.hasIdentifierProperty())
			return null;
		if (metadata instanceof EntityPersister) {
			((EntityPersister) metadata).getIdentifierGenerator();
		}
		return null;
	}

}
