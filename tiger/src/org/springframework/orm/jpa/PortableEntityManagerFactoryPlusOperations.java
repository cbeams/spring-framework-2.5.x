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

package org.springframework.orm.jpa;

/**
 * Common API for portable value adds supported by all vendors,
 * implemented by Spring-create EntityManagerFactory instances.
 * 
 * <p>Many of these features may make their way into future version of
 * the JPA API. In that case we will implement these methods to
 * use the standard method, and deprecate our own method.
 * 
 * <p>Spring-created EntityManagerFactory instances also implement
 * the EntityManagerFactoryInfo interface. That interface does not
 * extend EntityManagerFactory, so can also be returned by objects
 * that are not themselves EntityManagerFactorys, but know about the
 * configuration of an EntityManagerFactory.
 * 
 * @author Rod Johnson
 * @since 2.0
 * @see EntityManagerFactoryInfo
 */
public interface PortableEntityManagerFactoryPlusOperations {
	
	void evict(Class clazz);
	
	// all methods
	
	// EMFInfo?
	
	EntityManagerFactoryInfo getEntityManagerFactoryInfo();
	

}
