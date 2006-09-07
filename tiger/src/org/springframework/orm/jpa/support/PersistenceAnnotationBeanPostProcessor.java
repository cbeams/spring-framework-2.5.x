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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.ExtendedEntityManagerCreator;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * BeanPostProcessor that processes PersistenceUnit and PersistenceContext
 * annotations for injection of JPA interfaces. Any such annotated fields
 * or methods in any Spring-managed object will automatically be injected.
 * Will inject subinterfaces of EntityManager and EntityManagerFactory if possible.
 *
 * <p>May align with JSR-250 (Common Annotations for the Java Platform) in the
 * future. Note that this support can be used along with that support, as there
 * should be no conflict in implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.persistence.PersistenceUnit
 * @see javax.persistence.PersistenceContext
 */
public class PersistenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements BeanFactoryAware {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private ListableBeanFactory beanFactory;

	private Map<Class<?>, List<AnnotatedMember>> classMetadata = new HashMap<Class<?>, List<AnnotatedMember>>();


	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException("ListableBeanFactory required for EntityManagerFactory lookup");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}


	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {		
		List<AnnotatedMember> metadata = findClassMetadata(bean.getClass());
		for (AnnotatedMember member : metadata) {
			member.inject(bean);
		}
		return true;
	}

	private List<AnnotatedMember> findClassMetadata(Class<? extends Object> clazz) {
		synchronized (this.classMetadata) {
			List<AnnotatedMember> metadata = this.classMetadata.get(clazz);
			if (metadata == null) {
				final List<AnnotatedMember> newMetadata = new LinkedList<AnnotatedMember>();
				ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
					public void doWith(Field field) {
						addIfPresent(newMetadata, field);
					}
				});
				ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
					public void doWith(Method method) {
						addIfPresent(newMetadata, method);
					}
				});
				metadata = newMetadata;
				this.classMetadata.put(clazz, metadata);
			}
			return metadata;
		}
	}

	private void addIfPresent(List<AnnotatedMember> metadata, AccessibleObject ao) {
		PersistenceContext pc = ao.getAnnotation(PersistenceContext.class);
		if (pc != null) {
			Properties properties = null;
			PersistenceProperty[] pps = pc.properties();
			if (!ObjectUtils.isEmpty(pps)) {
				properties = new Properties();
				for (int i = 0; i < pps.length; i++) {
					PersistenceProperty pp = pps[i];
					properties.setProperty(pp.name(), pp.value());
				}
			}
			metadata.add(new AnnotatedMember(ao, pc.unitName(), pc.type(), properties));
		}
		else {
			PersistenceUnit pu = ao.getAnnotation(PersistenceUnit.class);
			if (pu != null) {
				metadata.add(new AnnotatedMember(ao, pu.unitName()));
			}
		}
	}


	/**
	 * Find an EntityManagerFactory with the given name in the current application context.
	 * @param emfName the name of the EntityManagerFactory
	 * @return the EntityManagerFactory
	 * @throws IllegalStateException if there is no such EntityManagerFactory in the context
	 */
	protected EntityManagerFactory findEntityManagerFactory(String emfName) throws IllegalStateException {
		if (StringUtils.hasLength(emfName)) {
			return findNamedEntityManagerFactory(emfName);
		}
		else {
			return findDefaultEntityManagerFactory();
		}
	}

	protected EntityManagerFactory findNamedEntityManagerFactory(String emfName) {
		String[] candidateNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, EntityManagerFactory.class);
		for (String candidateName : candidateNames) {
			EntityManagerFactory emf = (EntityManagerFactory) this.beanFactory.getBean(candidateName);
			String nameToCompare = candidateName;
			if (emf instanceof EntityManagerFactoryInfo) {
				EntityManagerFactoryInfo emfi = (EntityManagerFactoryInfo) emf;
				if (emfi.getPersistenceUnitName() != null) {
					nameToCompare = emfi.getPersistenceUnitName();
				}
			}
			if (emfName.equals(nameToCompare)) {
				return emf;
			}
		}
		throw new NoSuchBeanDefinitionException(EntityManagerFactory.class,
				"No EntityManagerFactory found for persistence unit name '" + emfName + "'");
	}

	protected EntityManagerFactory findDefaultEntityManagerFactory() {
		return (EntityManagerFactory) BeanFactoryUtils.beanOfTypeIncludingAncestors(
				this.beanFactory, EntityManagerFactory.class);
	}

	
	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class AnnotatedMember {

		private final AccessibleObject member;

		private final String unitName;

		private final PersistenceContextType type;

		private final Properties properties;


		public AnnotatedMember(AccessibleObject member, String unitName) {
			this(member, unitName, null, null);
		}

		public AnnotatedMember(AccessibleObject member, String unitName, PersistenceContextType type, Properties properties) {
			this.unitName = unitName;
			this.type = type;
			this.properties = properties;
			this.member = member;
			
			// Validate member type
			Class<?> memberType = getMemberType();
			if (!(EntityManagerFactory.class.isAssignableFrom(memberType) ||
					EntityManager.class.isAssignableFrom(memberType))) {
				throw new IllegalArgumentException("Cannot inject " + member + ": not a supported JPA type");
			}
		}


		public void inject(Object instance) {
			Object value = resolve();
			try {
				if (!this.member.isAccessible()) {
					this.member.setAccessible(true);
				}
				if (this.member instanceof Field) {
					((Field) this.member).set(instance, value);
				}
				else if (this.member instanceof Method) {
					((Method) this.member).invoke(instance, value);
				}
				else {
					throw new IllegalArgumentException("Cannot inject unknown AccessibleObject type " + this.member);
				}
			}
			catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Cannot inject member " + this.member, ex);
			}
			catch (InvocationTargetException ex) {
				// Method threw an exception
				throw new IllegalArgumentException("Attempt to inject setter method " + this.member +
						" resulted in an exception", ex);
			}
		}
		
		/**
		 * Return the type of the member, whether it's a field or a method.
		 */
		public Class<?> getMemberType() {
			if (member instanceof Field) {
				return ((Field) member).getType();
			}
			else if (member instanceof Method) {
				Method setter = (Method) member;
				if (setter.getParameterTypes().length != 1) {
					throw new IllegalArgumentException(
							"Supposed setter [" + this.member + "] must have 1 argument, not " +
							setter.getParameterTypes().length);
				}
				return setter.getParameterTypes()[0];
			}
			else {
				throw new IllegalArgumentException(
						"Unknown AccessibleObject type [" + this.member.getClass() +
						"]; can only inject setter methods and fields");
			}
		}

		/**
		 * Resolve the object against the application context.
		 */
		protected Object resolve() {
			// Resolves to EM or EMF.
			EntityManagerFactory emf = findEntityManagerFactory(this.unitName);
			if (EntityManagerFactory.class.isAssignableFrom(getMemberType())) {
				if (!getMemberType().isInstance(emf)) {
					throw new IllegalArgumentException("Cannot inject [" + this.member +
							"] with EntityManagerFactory [" + emf + "]: type mismatch");
				}
				return emf;
			}
			else {
				// We need to inject an EntityManager.
				if (this.type == PersistenceContextType.TRANSACTION) {
					// Inject a shared transactional EntityManager proxy.
					return SharedEntityManagerCreator.createSharedEntityManager(emf, getMemberType());
				}
				else {
					// Type is container-managed extended.
					return ExtendedEntityManagerCreator.createContainerManagedEntityManager(emf, this.properties);
				}
			}
		}
	}

}
