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

package org.springframework.dao.support;

import javax.persistence.PersistenceException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.support.DataAccessUtilsTests.MapPersistenceExceptionTranslator;
import org.springframework.stereotype.Repository;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 *
 */
public class PersistenceExceptionTranslationAdvisorTests extends TestCase {
	
	private RuntimeException doNotTranslate = new RuntimeException();
	
	private PersistenceException persistenceException1 = new PersistenceException();
	
	protected RepositoryInterface createProxy(RepositoryInterfaceImpl target, PersistenceExceptionTranslator pet) {
		ProxyFactory pf = new ProxyFactory(target);
		pf.addInterface(RepositoryInterface.class);
		PersistenceExceptionTranslationAdvisor peta = new PersistenceExceptionTranslationAdvisor(pet);
		pf.addAdvisor(peta);
		return (RepositoryInterface) pf.getProxy();
	}
	
	public void testNoTranslationNeeded() {
		RepositoryInterfaceImpl target = new RepositoryInterfaceImpl();
		ChainedPersistenceExceptionTranslator emptyPet = new ChainedPersistenceExceptionTranslator();
		RepositoryInterface ri = createProxy(target, emptyPet);
		
		ri.noThrowsClause();
		ri.throwsPersistenceException();		
	}
	
	public void testTranslationNotNeededForTheseExceptions() {
		RepositoryInterfaceImpl target = new StereotypedRepositoryInterfaceImpl();
		ChainedPersistenceExceptionTranslator emptyPet = new ChainedPersistenceExceptionTranslator();
		RepositoryInterface ri = createProxy(target, emptyPet);
		
		ri.noThrowsClause();
		ri.throwsPersistenceException();	
		target.setBehaviour(doNotTranslate);
		try {
			ri.noThrowsClause();
			fail();
		}
		catch (RuntimeException ex) {
			assertSame(doNotTranslate, ex);
		}
		try {
			ri.throwsPersistenceException();
			fail();
		}
		catch (RuntimeException ex) {
			assertSame(doNotTranslate, ex);
		}
	}
	
	public void testTranslationNeededForTheseExceptions() {
		RepositoryInterfaceImpl target = new StereotypedRepositoryInterfaceImpl();
		MapPersistenceExceptionTranslator mpet = new MapPersistenceExceptionTranslator();
		mpet.addTranslation(persistenceException1, 
				new InvalidDataAccessApiUsageException("", persistenceException1));
		RepositoryInterface ri = createProxy(target, mpet);
			
		target.setBehaviour(persistenceException1);
		try {
			ri.noThrowsClause();
			fail();
		}
		catch (DataAccessException ex) {
			// Expected
			assertSame(persistenceException1, ex.getCause());
		}
		catch (PersistenceException ex) {
			fail("Should have been translated");
		}
		
		try {
			ri.throwsPersistenceException();
			fail();
		}
		catch (PersistenceException ex) {
			assertSame(persistenceException1, ex);
		}
	}
	
	
	public interface RepositoryInterface {
		void noThrowsClause();
		void throwsPersistenceException() throws PersistenceException;
	}
	
	public static class RepositoryInterfaceImpl implements RepositoryInterface {
		private RuntimeException runtimeException;
				
		public void setBehaviour(RuntimeException rex) {
			this.runtimeException = rex;
		}
		
		public void noThrowsClause() {
			if (runtimeException != null) {
				throw runtimeException;
			}
		}
		
		public void throwsPersistenceException() throws PersistenceException {
			if (runtimeException != null) {
				throw runtimeException;
			}
		}
	}
	
	@Repository
	public static class StereotypedRepositoryInterfaceImpl extends RepositoryInterfaceImpl {
		// Extends above class just to add repository annotation
	}

}
