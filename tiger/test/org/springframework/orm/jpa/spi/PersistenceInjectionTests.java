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

package org.springframework.orm.jpa.spi;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;

import org.easymock.MockControl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBeanTests;

/**
 * Unit tests for persistence unit and persistence context injection
 * @author Rod Johnson
 *
 */
public class PersistenceInjectionTests extends AbstractEntityManagerFactoryBeanTests {
	
	private ApplicationContext applicationContext;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mockEmf.createEntityManager();
		Object mockEm = MockControl.createControl(EntityManager.class).getMock();
		// There are two extended contexts that require createEntityManager calls
		emfMc.setReturnValue(mockEm, 2);
		emfMc.replay();
		
		GenericApplicationContext sac = new GenericApplicationContext();
		
		BeanDefinitionRegistryBuilder bdrb = new BeanDefinitionRegistryBuilder(sac);
	
		// TODO really need to have an EntityManagerFactoryBean to get name
		sac.getDefaultListableBeanFactory().registerSingleton("entityManagerFactory", mockEmf);

		bdrb.register(BeanDefinitionBuilder.rootBeanDefinition(PersistenceAnnotationBeanPostProcessor.class));
		
		// Register persistence beans
		bdrb.register(DefaultPrivatePersistenceContextField.class.getName(), 
				BeanDefinitionBuilder.rootBeanDefinition(DefaultPrivatePersistenceContextField.class));
		bdrb.register(DefaultPublicPersistenceContextSetter.class.getName(), 
				BeanDefinitionBuilder.rootBeanDefinition(DefaultPublicPersistenceContextSetter.class));
		
		bdrb.register(DefaultPrivatePersistenceUnitField.class.getName(), 
				BeanDefinitionBuilder.rootBeanDefinition(DefaultPrivatePersistenceUnitField.class));
		bdrb.register(DefaultPublicPersistenceUnitSetter.class.getName(), 
				BeanDefinitionBuilder.rootBeanDefinition(DefaultPublicPersistenceUnitSetter.class));
		
		sac.refresh();
		
		this.applicationContext = sac;
	}
	
	public static class DefaultPrivatePersistenceContextField {
		
		@PersistenceContext
		private EntityManager em;
		
	}
	
	
	public static class DefaultPublicPersistenceContextSetter {
		
		private EntityManager em;
		
		@PersistenceContext(type=PersistenceContextType.EXTENDED)
		public void setEntityManager(EntityManager em) {
			this.em = em;
		}	
		
		public EntityManager getEntityManager() {
			return em;
		}
	}
	
	public static class DefaultPrivatePersistenceUnitField {
		@PersistenceUnit
		private EntityManagerFactory emf;
		
	}
	
	public static class DefaultPublicPersistenceUnitSetter {
		
		private EntityManagerFactory emf;
		
		@PersistenceUnit
		public void setEmf(EntityManagerFactory emf) {
			this.emf = emf;
		}
		
		public EntityManagerFactory getEmf() {
			return emf;
		}
	}
	
	public static class DefaultPublicPersistenceUnitSetterNamedPerson {
		
		private EntityManagerFactory emf;
		
		@PersistenceUnit(name="Person")
		public void setEmf(EntityManagerFactory emf) {
			this.emf = emf;
		}
		
		public EntityManagerFactory getEntityManagerFactory() {
			return emf;
		}
	}
	
	public static class FieldOfWrongTypeAnnotatedWithPersistenceUnit {
		
		@PersistenceUnit
		public String thisFieldIsOfTheWrongType;
		
	}
	
	public static class SetterOfWrongTypeAnnotatedWithPersistenceUnit {
		
		@PersistenceUnit
		public void setSomething(Comparable c) {
		}
	}
	
	public static class SetterWithNoArgs {
		
		@PersistenceUnit
		public void setSomething() {
		}
	}
	
	public void testDefaultPrivatePersistenceContextField() {
		DefaultPrivatePersistenceContextField dppc = (DefaultPrivatePersistenceContextField) applicationContext.getBean(DefaultPrivatePersistenceContextField.class.getName());
		assertNotNull("EntityManager was injected", dppc.em);
	}
	
	public void testDefaultPublicPersistenceContextSetter() {
		DefaultPublicPersistenceContextSetter dppc = (DefaultPublicPersistenceContextSetter) applicationContext.getBean(DefaultPublicPersistenceContextSetter.class.getName());
		assertNotNull("EntityManager was injected", dppc.em);
	}
	
	public void testDefaultPrivatePersistenceUnitField() {
		DefaultPrivatePersistenceUnitField dppc = (DefaultPrivatePersistenceUnitField) applicationContext.getBean(DefaultPrivatePersistenceUnitField.class.getName());
		assertNotNull("EntityManagerFactory was injected", dppc.emf);
	}
	
	public void testDefaultPublicPersistenceUnitSetter() {
		DefaultPublicPersistenceUnitSetter dppc = (DefaultPublicPersistenceUnitSetter) applicationContext.getBean(DefaultPublicPersistenceUnitSetter.class.getName());
		assertNotNull("EntityManagerFactory was injected", dppc.emf);
	}
	
	public void testFieldOfWrongTypeAnnotatedWithPersistenceUnit() {
		PersistenceAnnotationBeanPostProcessor babpp = new PersistenceAnnotationBeanPostProcessor();
		try {
			babpp.postProcessAfterInstantiation(new FieldOfWrongTypeAnnotatedWithPersistenceUnit(), "bean name does not matter");
			fail("Can't inject this field");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testSetterOfWrongTypeAnnotatedWithPersistenceUnit() {
		PersistenceAnnotationBeanPostProcessor babpp = new PersistenceAnnotationBeanPostProcessor();
		try {
			babpp.postProcessAfterInstantiation(new SetterOfWrongTypeAnnotatedWithPersistenceUnit(), "bean name does not matter");
			fail("Can't inject this setter");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testSetterWithNoArgs() {
		PersistenceAnnotationBeanPostProcessor babpp = new PersistenceAnnotationBeanPostProcessor();
		try {
			babpp.postProcessAfterInstantiation(new SetterWithNoArgs(), "bean name does not matter");
			fail("Can't inject this setter");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	
	public static class DefaultPrivatePersistenceContextFieldExtended {
		
		@PersistenceContext(type=PersistenceContextType.EXTENDED)
		private EntityManager em;
		
	}
	
	public void testNoPropertiesPassedIn() {
		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldExtended dppcf = new DefaultPrivatePersistenceContextFieldExtended();
		babpp.postProcessAfterInstantiation(dppcf, "bean name does not matter");
		assertNotNull(dppcf.em);
		emfMc.verify();
	}
	
	
	// TODO fix this
//	public static class DefaultPrivatePersistenceContextFieldExtendedWithProps {
//		
//		@PersistenceContext(
//				type=PersistenceContextType.EXTENDED, 
//				properties=PersistenceProperty)
//		private EntityManager em;
//	}
//	
//	public void testPropertiesPassedIn() {
//		Properties props = new Properties();
//		props.put("foo", "bar");
//		mockEmf.createEntityManager(props);
//		emfMc.setReturnValue(MockControl.createControl(EntityManager.class).getMock(), 1);
//		emfMc.replay();
//		
//		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
//		DefaultPrivatePersistenceContextFieldExtendedWithProps dppcf = new DefaultPrivatePersistenceContextFieldExtendedWithProps();
//		babpp.postProcessAfterInstantiation(dppcf, "bean name does not matter");
//		assertNotNull(dppcf.em);
//		emfMc.verify();
//	}
	
	
	/**
	 * Returns mock EMF
	 */
	private final class MockPersistenceAnnotationBeanPostProcessor extends PersistenceAnnotationBeanPostProcessor {
		@Override
		protected EntityManagerFactory findEntityManagerFactoryWithName(String emfName) throws NoSuchBeanDefinitionException {
			return mockEmf;
		}
	}

	
}
