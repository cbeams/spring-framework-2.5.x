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

package org.springframework.orm.jpa.support;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import org.easymock.MockControl;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBeanTests;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Unit tests for persistence unit and persistence context injection.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class PersistenceInjectionTests extends AbstractEntityManagerFactoryBeanTests {

	private ApplicationContext applicationContext;


	private void initApplicationContext() {
		mockEmf.createEntityManager();
		Object mockEm = MockControl.createControl(EntityManager.class).getMock();
		// There are two extended contexts that require createEntityManager calls.
		emfMc.setReturnValue(mockEm, 2);
		emfMc.replay();

		GenericApplicationContext gac = new GenericApplicationContext();

		gac.getDefaultListableBeanFactory().registerSingleton("entityManagerFactory", mockEmf);

		gac.registerBeanDefinition("annotationProcessor",
				new RootBeanDefinition(PersistenceAnnotationBeanPostProcessor.class));

		// Register persistence beans
		gac.registerBeanDefinition(DefaultPrivatePersistenceContextField.class.getName(),
				new RootBeanDefinition(DefaultPrivatePersistenceContextField.class));
		gac.registerBeanDefinition(DefaultPublicPersistenceContextSetter.class.getName(),
				new RootBeanDefinition(DefaultPublicPersistenceContextSetter.class));

		gac.registerBeanDefinition(DefaultPrivatePersistenceUnitField.class.getName(),
				new RootBeanDefinition(DefaultPrivatePersistenceUnitField.class));
		gac.registerBeanDefinition(DefaultPublicPersistenceUnitSetter.class.getName(),
				new RootBeanDefinition(DefaultPublicPersistenceUnitSetter.class));

		gac.refresh();

		this.applicationContext = gac;
	}

	public void testDefaultPrivatePersistenceContextField() {
		initApplicationContext();
		DefaultPrivatePersistenceContextField dppc = (DefaultPrivatePersistenceContextField) applicationContext.getBean(
				DefaultPrivatePersistenceContextField.class.getName());
		assertNotNull("EntityManager was injected", dppc.em);
	}

	public void testDefaultPublicPersistenceContextSetter() {
		initApplicationContext();
		DefaultPublicPersistenceContextSetter dppc = (DefaultPublicPersistenceContextSetter) applicationContext.getBean(
				DefaultPublicPersistenceContextSetter.class.getName());
		assertNotNull("EntityManager was injected", dppc.em);
	}

	public void testDefaultPrivatePersistenceUnitField() {
		initApplicationContext();
		DefaultPrivatePersistenceUnitField dppc = (DefaultPrivatePersistenceUnitField) applicationContext.getBean(
				DefaultPrivatePersistenceUnitField.class.getName());
		assertNotNull("EntityManagerFactory was injected", dppc.emf);
	}

	public void testDefaultPublicPersistenceUnitSetter() {
		initApplicationContext();
		DefaultPublicPersistenceUnitSetter dppc = (DefaultPublicPersistenceUnitSetter) applicationContext.getBean(
				DefaultPublicPersistenceUnitSetter.class.getName());
		assertNotNull("EntityManagerFactory was injected", dppc.emf);
	}

	public void testFieldOfWrongTypeAnnotatedWithPersistenceUnit() {
		PersistenceAnnotationBeanPostProcessor babpp = new PersistenceAnnotationBeanPostProcessor();
		try {
			babpp.postProcessAfterInstantiation(new FieldOfWrongTypeAnnotatedWithPersistenceUnit(),
					"bean name does not matter");
			fail("Can't inject this field");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}

	public void testSetterOfWrongTypeAnnotatedWithPersistenceUnit() {
		PersistenceAnnotationBeanPostProcessor babpp = new PersistenceAnnotationBeanPostProcessor();
		try {
			babpp.postProcessAfterInstantiation(new SetterOfWrongTypeAnnotatedWithPersistenceUnit(),
					"bean name does not matter");
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

	public void testNoPropertiesPassedIn() {
		mockEmf.createEntityManager();
		emfMc.setReturnValue(MockControl.createControl(EntityManager.class).getMock(), 1);
		emfMc.replay();

		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldExtended dppcf = new DefaultPrivatePersistenceContextFieldExtended();
		babpp.postProcessAfterInstantiation(dppcf, "bean name does not matter");
		assertNotNull(dppcf.em);
		emfMc.verify();
	}

	public void testPropertiesPassedIn() {
		Properties props = new Properties();
		props.put("foo", "bar");
		mockEmf.createEntityManager(props);
		emfMc.setReturnValue(MockControl.createControl(EntityManager.class).getMock(), 1);
		emfMc.replay();

		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldExtendedWithProps dppcf =
				new DefaultPrivatePersistenceContextFieldExtendedWithProps();
		babpp.postProcessAfterInstantiation(dppcf, "bean name does not matter");
		assertNotNull(dppcf.em);
		emfMc.verify();
	}

	public void testPropertiesForTransactionalEM() {
		Properties props = new Properties();
		props.put("foo", "bar");
		MockControl emC = MockControl.createControl(EntityManager.class);
		EntityManager em = (EntityManager) emC.getMock();
		emfMc.expectAndReturn(mockEmf.createEntityManager(props), em);
		emC.expectAndReturn(em.getDelegate(), new Object());
		em.close();

		emfMc.replay();
		emC.replay();

		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldWithProperties transactionalField =
				new DefaultPrivatePersistenceContextFieldWithProperties();
		babpp.postProcessAfterInstantiation(transactionalField, "bean name does not matter");

		assertNotNull(transactionalField.em);
		assertNotNull(transactionalField.em.getDelegate());

		emfMc.verify();
		emC.verify();
	}

	/**
	 * Binds an EMF to the thread and tests if EM with different properties
	 * generate new EMs or not
	 * 
	 */
	public void testPropertiesForSharedEM1() {
		Properties props = new Properties();
		props.put("foo", "bar");
		MockControl emC = MockControl.createControl(EntityManager.class);
		EntityManager em = (EntityManager) emC.getMock();
		// only one call made  - the first EM definition wins (in this case the one w/ the properties)
		emfMc.expectAndReturn(mockEmf.createEntityManager(props), em);
		emC.expectAndReturn(em.getDelegate(), new Object(), 2);
		em.close();

		emfMc.replay();
		emC.replay();

		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldWithProperties transactionalFieldWithProperties =
				new DefaultPrivatePersistenceContextFieldWithProperties();
		DefaultPrivatePersistenceContextField transactionalField = new DefaultPrivatePersistenceContextField();

		babpp.postProcessAfterInstantiation(transactionalFieldWithProperties, "bean name does not matter");
		babpp.postProcessAfterInstantiation(transactionalField, "bean name does not matter");

		assertNotNull(transactionalFieldWithProperties.em);
		assertNotNull(transactionalField.em);
		// the EM w/ properties will be created
		assertNotNull(transactionalFieldWithProperties.em.getDelegate());
		// bind em to the thread now since it's created
		try {
			TransactionSynchronizationManager.bindResource(mockEmf, new EntityManagerHolder(em));
			assertNotNull(transactionalField.em.getDelegate());

			emfMc.verify();
			emC.verify();
		}
		catch (IllegalStateException e) {
			TransactionSynchronizationManager.unbindResource(mockEmf);
		}
	}
	
	public void testPropertiesForSharedEM2() {
		Properties props = new Properties();
		props.put("foo", "bar");
		MockControl emC = MockControl.createControl(EntityManager.class);
		EntityManager em = (EntityManager) emC.getMock();
		// only one call made  - the first EM definition wins (in this case the one w/o the properties)
		emfMc.expectAndReturn(mockEmf.createEntityManager(), em);
		emC.expectAndReturn(em.getDelegate(), new Object(), 2);
		em.close();

		emfMc.replay();
		emC.replay();

		PersistenceAnnotationBeanPostProcessor babpp = new MockPersistenceAnnotationBeanPostProcessor();
		DefaultPrivatePersistenceContextFieldWithProperties transactionalFieldWithProperties =
				new DefaultPrivatePersistenceContextFieldWithProperties();
		DefaultPrivatePersistenceContextField transactionalField = new DefaultPrivatePersistenceContextField();

		babpp.postProcessAfterInstantiation(transactionalFieldWithProperties, "bean name does not matter");
		babpp.postProcessAfterInstantiation(transactionalField, "bean name does not matter");

		assertNotNull(transactionalFieldWithProperties.em);
		assertNotNull(transactionalField.em);
		// the EM w/o properties will be created
		assertNotNull(transactionalField.em.getDelegate());
		// bind em to the thread now since it's created
		try {
			TransactionSynchronizationManager.bindResource(mockEmf, new EntityManagerHolder(em));
			assertNotNull(transactionalFieldWithProperties.em.getDelegate());

			emfMc.verify();
			emC.verify();
		}
		catch (IllegalStateException e) {
			TransactionSynchronizationManager.unbindResource(mockEmf);
		}
	}


	private final class MockPersistenceAnnotationBeanPostProcessor extends PersistenceAnnotationBeanPostProcessor {

		@Override
		protected EntityManagerFactory findEntityManagerFactory(String emfName) throws NoSuchBeanDefinitionException {
			return mockEmf;
		}
	}


	public static class DefaultPrivatePersistenceContextField {

		@PersistenceContext()
		private EntityManager em;
	}


	public static class DefaultPrivatePersistenceContextFieldWithProperties {

		@PersistenceContext(properties = { @PersistenceProperty(name = "foo", value = "bar") })
		private EntityManager em;
	}


	public static class DefaultPublicPersistenceContextSetter {

		private EntityManager em;

		@PersistenceContext(type = PersistenceContextType.EXTENDED)
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

		@PersistenceUnit(name = "Person")
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


	public static class DefaultPrivatePersistenceContextFieldExtended {

		@PersistenceContext(type = PersistenceContextType.EXTENDED)
		private EntityManager em;
	}


	public static class DefaultPrivatePersistenceContextFieldExtendedWithProps {

		@PersistenceContext(type = PersistenceContextType.EXTENDED, properties = { @PersistenceProperty(name = "foo", value = "bar") })
		private EntityManager em;
	}

}
