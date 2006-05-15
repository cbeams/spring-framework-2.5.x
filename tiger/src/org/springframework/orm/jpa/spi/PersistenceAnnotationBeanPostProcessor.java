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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.support.ExtendedEntityManagerFactory;
import org.springframework.orm.jpa.support.SharedEntityManagerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * BeanPostProcessor that processes PersistenceUnit and PersistenceContext
 * annotations for injection of JPA interfaces. Any such annotated fields
 * or methods in any Spring-managed object will automatically be injected.
 * Will inject subinterfaces of EntityManager and EntityManagerFactory if possible.
 * 
 * <p>May align with JSR-250 (Common Annotations for JavaPlatform) subproject in 
 * future. Note that this support can be used along with that support, as there
 * should be no conflict in implementation.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class PersistenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter 
														implements ApplicationContextAware {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Map<Class<?>, List<AnnotatedMember>> classMetadata = new HashMap<Class<?>, List<AnnotatedMember>>();

	private Map<String, EntityManagerFactory> entityManagersByName;
	
	private EntityManagerFactory uniqueEntityManagerFactory;
	
	private ApplicationContext applicationContext;
	

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	
	/**
	 * Lazily initialize entity manager map
	 *
	 */
	private synchronized void initMapsIfNecessary() {
		if (entityManagersByName == null) {
			entityManagersByName = new HashMap<String, EntityManagerFactory>();
			// Look for named EntityManagers
			for (String emfName : applicationContext.getBeanNamesForType(PortableEntityManagerFactoryPlus.class)) {
				PortableEntityManagerFactoryPlus emf = (PortableEntityManagerFactoryPlus) applicationContext.getBean(emfName);								
				if (emf.getEntityManagerFactoryInfo().getPersistenceUnitName() != null) {
					entityManagersByName.put(emf.getEntityManagerFactoryInfo().getPersistenceUnitName(), 
							(EntityManagerFactory) applicationContext.getBean(emfName));
				}
			}
			
			if (entityManagersByName.isEmpty()) {
				// Try to find a unique EntityManagerFactory
				// Will work even if not using Spring EntityManagerFactory
				String[] emfNames = applicationContext.getBeanNamesForType(EntityManagerFactory.class);
				if (emfNames.length == 1) {
					uniqueEntityManagerFactory = (EntityManagerFactory) applicationContext.getBean(emfNames[0]);
				}
			}
			else if (entityManagersByName.size() == 1) {
				uniqueEntityManagerFactory = entityManagersByName.values().iterator().next();
			}
			
			if (entityManagersByName.isEmpty() && uniqueEntityManagerFactory == null) {
				log.warn("No named EntityManagerFactorys defined and not exactly one anonymous one: cannot inject");
			}
		}
	}
	
	/**
	 * Find an EntityManagerFactory with the given name in the current
	 * application context
	 * @param emfName name of the EntityManagerFactory
	 * @return the EntityManagerFactory or throw NoSuchBeanDefinitionException
	 * @throws NoSuchBeanDefinitionException if there is no such EntityManagerFactory
	 * in the context
	 */
	protected EntityManagerFactory findEntityManagerFactoryWithName(String emfName) throws NoSuchBeanDefinitionException {
		initMapsIfNecessary();
		
		if (emfName == null || "".equals(emfName)) {
			if (uniqueEntityManagerFactory != null) {
				return uniqueEntityManagerFactory;
			}
			else {
				throw new NoSuchBeanDefinitionException("No EntityManagerFactory name given and factory contains several");
			}
		}
		EntityManagerFactory namedEmf = entityManagersByName.get(emfName);
		if (namedEmf == null) {
			throw new NoSuchBeanDefinitionException("No EntityManagerFactory found for persistence unit name '" + emfName + "'");
		}
		return namedEmf;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {		
		List<AnnotatedMember> metadata = findClassMetadata(bean.getClass());
		for (AnnotatedMember member : metadata) {
			member.inject(bean);
		}
		return true;
	}

	
	private synchronized List<AnnotatedMember> findClassMetadata(Class<? extends Object> clazz) {
		List<AnnotatedMember> metadata = classMetadata.get(clazz);
		if (metadata == null) {
			final List<AnnotatedMember> newMetadata = new LinkedList<AnnotatedMember>();

			ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
				public void doWith(Field f) throws IllegalArgumentException, IllegalAccessException {
					addIfPresent(newMetadata, f);
				}
			});

			// TODO is it correct to walk up the hierarchy for methods? Otherwise inheritance
			// is implied? CL to resolve
			ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
				public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
					addIfPresent(newMetadata, m);
				}
			});

			metadata = newMetadata;
			classMetadata.put(clazz, metadata);
		}
		return metadata;
	}
	
	
	private void addIfPresent(List<AnnotatedMember> metadata, AccessibleObject ao) {
		PersistenceContext pc = ao.getAnnotation(PersistenceContext.class);
		if (pc != null) {
			metadata.add(new AnnotatedMember(pc.unitName(), pc.type(), ao));
		}
		else {
			PersistenceUnit pu = ao.getAnnotation(PersistenceUnit.class);
			if (pu != null) {
				metadata.add(new AnnotatedMember(pu.unitName(), null, ao));
			}
		}
	}

	
	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class AnnotatedMember {

		private final String unitName;

		private final PersistenceContextType type;

		private final AccessibleObject member;

		public AnnotatedMember(String unitName, PersistenceContextType type, AccessibleObject member) {
			this.unitName = unitName;
			this.type = type;
			this.member = member;
			
			// Validate member type
			Class<?> memberType = getMemberType();
			if (!(EntityManagerFactory.class.isAssignableFrom(memberType) ||
					EntityManager.class.isAssignableFrom(memberType))) {
				throw new IllegalArgumentException("Cannot inject " + member + ": not a JPA type");
			}
		}

		public void inject(Object instance) {
			Object value = resolve();
			try {
				if (!member.isAccessible()) {
					member.setAccessible(true);
				}
				if (member instanceof Field) {
					((Field) member).set(instance, value);
				}
				else if (member instanceof Method) {
					((Method) member).invoke(instance, value);
				}
				else {
					throw new IllegalArgumentException("Cannot inject unknown AccessibleObject type " + member);
				}
			}
			catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Cannot inject member " + member, ex);
			}
			catch (InvocationTargetException ex) {
				// Method threw an exception
				throw new IllegalArgumentException("Attempt to inject setter method " + member +
						" resulted in an exception", ex);
			}
		}
		
		/**
		 * @return the type of the member, whether it's a field or a method
		 */
		public Class<?> getMemberType() {
			if (member instanceof Field) {
				return ((Field) member).getType();
			}
			else if (member instanceof Method) {
				Method setter = (Method) member;
				if (setter.getParameterTypes().length != 1) {
					throw new IllegalArgumentException("Supposed setter " + member + " must have 1 argument, not " +  
							setter.getParameterTypes().length);
				}
				return setter.getParameterTypes()[0];
			}
			else {
				throw new IllegalArgumentException("Unknown AccessibleObject type " + member.getClass() + 
						"; Can only inject settermethods or fields");
			}
		}

		// Resolve the object against the application context
		protected Object resolve() {
			// Resolves to EM or EMF
			EntityManagerFactory emf = findEntityManagerFactoryWithName(unitName);
			EntityManagerFactoryInfo emfi;
			if (emf instanceof PortableEntityManagerFactoryPlus) {
				emfi = ((PortableEntityManagerFactoryPlus) emf).getEntityManagerFactoryInfo();
			}
			else {
				emfi = null;
			}
			
			if (EntityManagerFactory.class.isAssignableFrom(getMemberType())) {
				return emf;
			}
			else {
				// We need to inject an EntityManager
				if (type == PersistenceContextType.TRANSACTION) {
					// Inject a shared transactional EntityManager proxy
					return SharedEntityManagerFactory.createEntityManagerProxy(
							getClass().getClassLoader(),
							emf,
							getMemberType());
				}
				else {
					// Type is container-managed extended
					boolean jta = false;
					return ExtendedEntityManagerFactory.createContainerManagedEntityManager(
							getClass().getClassLoader(),
							//new Class[] { getMemberType() },
							emf,
							emfi,
							jta);
				}
			}
		}
	}

}
