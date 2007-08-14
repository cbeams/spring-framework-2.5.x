/*
 * Copyright 2002-2007 the original author or authors.
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.ExtendedEntityManagerCreator;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * BeanPostProcessor that processes {@link javax.persistence.PersistenceUnit}
 * and {@link javax.persistence.PersistenceContext} annotations, for injection of
 * the corresponding JPA resources {@link javax.persistence.EntityManagerFactory}
 * and {@link javax.persistence.EntityManager}. Any such annotated fields or methods
 * in any Spring-managed object will automatically be injected.
 *
 * <p>This post-processor will inject sub-interfaces of <code>EntityManagerFactory</code>
 * and <code>EntityManager</code> if the annotated fields or methods are declared as such.
 * The actual type will be verified early, with the exception of a shared ("transactional")
 * <code>EntityManager</code> reference, where type mismatches might be detected as late
 * as on the first actual invocation.
 *
 * <p>Note: In the present implementation, PersistenceAnnotationBeanPostProcessor
 * only supports <code>@PersistenceUnit</code> and <code>@PersistenceContext</code>
 * with the "unitName" attribute, or no attribute at all (for the default unit).
 * If those annotations are present with the "name" attribute at the class level,
 * they will simply be ignored, since those only serve as deployment hint
 * (as per the Java EE 5 specification).
 *
 * <p>This post-processor can either obtain EntityManagerFactory beans defined
 * in the Spring application context (the default), or obtain EntityManagerFactory
 * references from JNDI ("persistence unit references"). In the bean case,
 * the persistence unit name will be matched against the actual deployed unit,
 * with the bean name used as fallback unit name if no deployed name found.
 * Typically, Spring's {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}
 * will be used for setting up such EntityManagerFactory beans. Alternatively,
 * such beans may also be obtained from JNDI, e.g. using the <code>jee:jndi-lookup</code>
 * XML configuration element (with the bean name matching the requested unit name).
 * In both cases, the post-processor definition will look as simple as this:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor"/&gt;</pre>
 *
 * In the JNDI case, specify the corresponding JNDI names in this post-processor's
 * {@link #setPersistenceUnits "persistenceUnits" map}, typically with matching
 * <code>persistence-unit-ref</code> entries in the Java EE deployment descriptor.
 * By default, those names are considered as resource references (according to the
 * Java EE resource-ref convention), located underneath the "java:comp/env/" namespace.
 * For example:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor"&gt;
 *   &lt;property name="persistenceUnits"&gt;
 *     &lt;map/gt;
 *       &lt;entry key="unit1" value="persistence/unit1"/&gt;
 *       &lt;entry key="unit2" value="persistence/unit2"/&gt;
 *     &lt;/map/gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * In this case, the specified persistence units will always be resolved in JNDI
 * rather than as Spring-defined beans. The entire persistence unit deployment,
 * including the weaving of persistent classes, is then up to the Java EE server.
 * Persistence contexts (i.e. EntityManager references) will be built based on
 * those server-provided EntityManagerFactory references, using Spring's own
 * transaction synchronization facilities for transactional EntityManager handling
 * (typically with Spring's <code>@Transactional</code> annotation for demarcation
 * and {@link org.springframework.transaction.jta.JtaTransactionManager} as backend).
 *
 * <p>If you prefer the Java EE server's own EntityManager handling, specify entries
 * in this post-processor's {@link #setPersistenceContexts "persistenceContexts" map}
 * (or {@link #setExtendedPersistenceContexts "extendedPersistenceContexts" map},
 * typically with matching <code>persistence-context-ref</code> entries in the
 * Java EE deployment descriptor. For example:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor"&gt;
 *   &lt;property name="persistenceContexts"&gt;
 *     &lt;map/gt;
 *       &lt;entry key="unit1" value="persistence/context1"/&gt;
 *       &lt;entry key="unit2" value="persistence/context2"/&gt;
 *     &lt;/map/gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * If the application only obtains EntityManager references in the first place,
 * this is all you need to specify. If you need EntityManagerFactory references
 * as well, specify entries for both "persistenceUnits" and "persistenceContexts",
 * pointing to matching JNDI locations.
 *
 * <p><b>NOTE: In general, do not inject EXTENDED EntityManagers into STATELESS beans,
 * i.e. do not use <code>@PersistenceContext</code> with type <code>EXTENDED</code> in
 * Spring beans defined with scope 'singleton' (Spring's default scope).</b>
 * Extended EntityManagers are <i>not</i> thread-safe, hence they must not be used
 * in concurrently accessed beans (which Spring-managed singletons usually are).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.persistence.PersistenceUnit
 * @see javax.persistence.PersistenceContext
 */
public class PersistenceAnnotationBeanPostProcessor extends JndiLocatorSupport
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

	private Map<String, String> persistenceUnits;

	private Map<String, String> persistenceContexts;

	private Map<String, String> extendedPersistenceContexts;

	private String defaultPersistenceUnitName = "";

	private ListableBeanFactory beanFactory;

	private final Map<Class<?>, List<AnnotatedMember>> annotationMetadataCache =
			new HashMap<Class<?>, List<AnnotatedMember>>();


	public PersistenceAnnotationBeanPostProcessor() {
		setResourceRef(true);
	}


	/**
	 * Specify the persistence units for EntityManagerFactory lookups,
	 * as a Map from persistence unit name to persistence unit JNDI name
	 * (which needs to resolve to an EntityManagerFactory instance).
	 * <p>JNDI names specified here should refer to <code>persistence-unit-ref</code>
	 * entries in the Java EE deployment descriptor, matching the target persistence unit.
	 * <p>In case of no unit name specified in the annotation, the specified value
	 * for the {@link #setDefaultPersistenceUnitName default persistence unit}
	 * will be taken (by default, the value mapped to the empty String),
	 * or simply the single persistence unit if there is only one.
	 * <p>This is mainly intended for use in a Java EE 5 environment, with all
	 * lookup driven by the standard JPA annotations, and all EntityManagerFactory
	 * references obtained from JNDI. No separate EntityManagerFactory bean
	 * definitions are necessary in such a scenario.
	 * <p>If no corresponding "persistenceContexts"/"extendedPersistenceContexts"
	 * are specified, <code>@PersistenceContext</code> will be resolved to
	 * EntityManagers built on top of the EntityManagerFactory defined here.
	 * Note that those will be Spring-managed EntityManagers, which implement
	 * transaction synchronization based on Spring's facilities.
	 * If you prefer the Java EE 5 server's own EntityManager handling,
	 * specify corresponding "persistenceContexts"/"extendedPersistenceContexts".
	 */
	public void setPersistenceUnits(Map<String, String> persistenceUnits) {
		this.persistenceUnits = persistenceUnits;
	}

	/**
	 * Specify the <i>transactional</i> persistence contexts for EntityManager lookups,
	 * as a Map from persistence unit name to persistence context JNDI name
	 * (which needs to resolve to an EntityManager instance).
	 * <p>JNDI names specified here should refer to <code>persistence-context-ref</code>
	 * entries in the Java EE deployment descriptors, matching the target persistence unit
	 * and being set up with persistence context type <code>Transaction</code>.
	 * <p>In case of no unit name specified in the annotation, the specified value
	 * for the {@link #setDefaultPersistenceUnitName default persistence unit}
	 * will be taken (by default, the value mapped to the empty String),
	 * or simply the single persistence unit if there is only one.
	 * <p>This is mainly intended for use in a Java EE 5 environment, with all
	 * lookup driven by the standard JPA annotations, and all EntityManager
	 * references obtained from JNDI. No separate EntityManagerFactory bean
	 * definitions are necessary in such a scenario, and all EntityManager
	 * handling is done by the Java EE 5 server itself.
	 */
	public void setPersistenceContexts(Map<String, String> persistenceContexts) {
		this.persistenceContexts = persistenceContexts;
	}

	/**
	 * Specify the <i>extended</i> persistence contexts for EntityManager lookups,
	 * as a Map from persistence unit name to persistence context JNDI name
	 * (which needs to resolve to an EntityManager instance).
	 * <p>JNDI names specified here should refer to <code>persistence-context-ref</code>
	 * entries in the Java EE deployment descriptors, matching the target persistence unit
	 * and being set up with persistence context type <code>Extended</code>.
	 * <p>In case of no unit name specified in the annotation, the specified value
	 * for the {@link #setDefaultPersistenceUnitName default persistence unit}
	 * will be taken (by default, the value mapped to the empty String),
	 * or simply the single persistence unit if there is only one.
	 * <p>This is mainly intended for use in a Java EE 5 environment, with all
	 * lookup driven by the standard JPA annotations, and all EntityManager
	 * references obtained from JNDI. No separate EntityManagerFactory bean
	 * definitions are necessary in such a scenario, and all EntityManager
	 * handling is done by the Java EE 5 server itself.
	 */
	public void setExtendedPersistenceContexts(Map<String, String> extendedPersistenceContexts) {
		this.extendedPersistenceContexts = extendedPersistenceContexts;
	}

	/**
	 * Specify the default persistence unit name, to be used in case
	 * of no unit name specified in an <code>@PersistenceUnit</code> /
	 * <code>@PersistenceContext</code> annotation.
	 * <p>This is mainly intended for lookups in the application context,
	 * indicating the target persistence unit name (typically matching
	 * the bean name), but also applies to lookups in the
	 * {@link #setPersistenceUnits "persistenceUnits"} /
	 * {@link #setPersistenceContexts "persistenceContexts"} /
	 * {@link #setExtendedPersistenceContexts "extendedPersistenceContexts"} map,
	 * avoiding the need for duplicated mappings for the empty String there.
	 * <p>Default is to check for a single EntityManagerFactory bean
	 * in the Spring application context, if any. If there are multiple
	 * such factories, either specify this default persistence unit name
	 * or explicitly refer to named persistence units in your annotations.
	 */
	public void setDefaultPersistenceUnitName(String unitName) {
		this.defaultPersistenceUnitName = (unitName != null ? unitName : "");
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ListableBeanFactory) {
			this.beanFactory = (ListableBeanFactory) beanFactory;
		}
	}


	public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
		return null;
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		List<AnnotatedMember> metadata = findAnnotationMetadata(bean.getClass());
		for (AnnotatedMember member : metadata) {
			member.inject(bean);
		}
		return true;
	}

	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}


	private List<AnnotatedMember> findAnnotationMetadata(Class clazz) {
		synchronized (this.annotationMetadataCache) {
			List<AnnotatedMember> metadata = this.annotationMetadataCache.get(clazz);
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
				this.annotationMetadataCache.put(clazz, metadata);
			}
			return metadata;
		}
	}

	private void addIfPresent(List<AnnotatedMember> metadata, Member member) {
		AnnotatedElement ae = (AnnotatedElement) member;
		PersistenceContext pc = ae.getAnnotation(PersistenceContext.class);
		PersistenceUnit pu = ae.getAnnotation(PersistenceUnit.class);
		if (pc != null) {
			if (pu != null) {
				throw new IllegalStateException(
						"Method may only be annotated with either @PersistenceContext or @PersistenceUnit, not both");
			}
			if (member instanceof Method && ((Method) member).getParameterTypes().length != 1) {
				throw new IllegalStateException("PersistenceContext annotation requires a single-arg method: " + member);
			}
			Properties properties = null;
			PersistenceProperty[] pps = pc.properties();
			if (!ObjectUtils.isEmpty(pps)) {
				properties = new Properties();
				for (int i = 0; i < pps.length; i++) {
					PersistenceProperty pp = pps[i];
					properties.setProperty(pp.name(), pp.value());
				}
			}
			metadata.add(new AnnotatedMember(member, pc.unitName(), pc.type(), properties));
		}
		else if (pu != null) {
			if (member instanceof Method && ((Method) member).getParameterTypes().length != 1) {
				throw new IllegalStateException("PersistenceUnit annotation requires a single-arg method: " + member);
			}
			metadata.add(new AnnotatedMember(member, pu.unitName()));
		}
	}


	/**
	 * Return a specified persistence unit for the given unit name,
	 * as defined through the "persistenceUnits" map.
	 * @param unitName the name of the persistence unit
	 * @return the corresponding EntityManagerFactory,
	 * or <code>null</code> if none found
	 * @see #setPersistenceUnits
	 */
	protected EntityManagerFactory getPersistenceUnit(String unitName) {
		if (this.persistenceUnits != null) {
			String unitNameForLookup = (unitName != null ? unitName : "");
			if ("".equals(unitNameForLookup)) {
				unitNameForLookup = this.defaultPersistenceUnitName;
			}
			String jndiName = this.persistenceUnits.get(unitNameForLookup);
			if (jndiName == null && "".equals(unitNameForLookup) && this.persistenceUnits.size() == 1) {
				jndiName = this.persistenceUnits.values().iterator().next();
			}
			if (jndiName != null) {
				try {
					return (EntityManagerFactory) lookup(jndiName, EntityManagerFactory.class);
				}
				catch (NamingException ex) {
					throw new IllegalStateException("Could not obtain EntityManagerFactory [" + jndiName + "] from JNDI", ex);
				}
			}
		}
		return null;
	}

	/**
	 * Return a specified persistence context for the given unit name, as defined
	 * through the "persistenceContexts" (or "extendedPersistenceContexts") map.
	 * @param unitName the name of the persistence unit
	 * @param extended whether to obtain an extended persistence context
	 * @return the corresponding EntityManager, or <code>null</code> if none found
	 * @see #setPersistenceContexts
	 * @see #setExtendedPersistenceContexts
	 */
	protected EntityManager getPersistenceContext(String unitName, boolean extended) {
		Map<String, String> contexts = (extended ? this.extendedPersistenceContexts : this.persistenceContexts);
		if (contexts != null) {
			String unitNameForLookup = (unitName != null ? unitName : "");
			if ("".equals(unitNameForLookup)) {
				unitNameForLookup = this.defaultPersistenceUnitName;
			}
			String jndiName = contexts.get(unitNameForLookup);
			if (jndiName == null && "".equals(unitNameForLookup) && contexts.size() == 1) {
				jndiName = contexts.values().iterator().next();
			}
			if (jndiName != null) {
				try {
					return (EntityManager) lookup(jndiName, EntityManager.class);
				}
				catch (NamingException ex) {
					throw new IllegalStateException("Could not obtain EntityManager [" + jndiName + "] from JNDI", ex);
				}
			}
		}
		return null;
	}

	/**
	 * Find an EntityManagerFactory with the given name in the current Spring
	 * application context, falling back to a single default EntityManagerFactory
	 * (if any) in case of no unit name specified.
	 * @param unitName the name of the persistence unit (may be <code>null</code> or empty)
	 * @return the EntityManagerFactory
	 * @throws NoSuchBeanDefinitionException if there is no such EntityManagerFactory in the context
	 */
	protected EntityManagerFactory findEntityManagerFactory(String unitName) throws NoSuchBeanDefinitionException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("ListableBeanFactory required for EntityManagerFactory lookup");
		}
		String unitNameForLookup = (unitName != null ? unitName : "");
		if ("".equals(unitNameForLookup)) {
			unitNameForLookup = this.defaultPersistenceUnitName;
		}
		if (!"".equals(unitNameForLookup)) {
			return findNamedEntityManagerFactory(unitNameForLookup);
		}
		else {
			return findDefaultEntityManagerFactory();
		}
	}

	/**
	 * Find an EntityManagerFactory with the given name in the current
	 * Spring application context.
	 * @param unitName the name of the persistence unit (never empty)
	 * @return the EntityManagerFactory
	 * @throws NoSuchBeanDefinitionException if there is no such EntityManagerFactory in the context
	 */
	protected EntityManagerFactory findNamedEntityManagerFactory(String unitName)
			throws NoSuchBeanDefinitionException {

		return EntityManagerFactoryUtils.findEntityManagerFactory(this.beanFactory, unitName);
	}

	/**
	 * Find a single default EntityManagerFactory in the Spring application context.
	 * @return the default EntityManagerFactory
	 * @throws NoSuchBeanDefinitionException if there is no single EntityManagerFactory in the context
	 */
	protected EntityManagerFactory findDefaultEntityManagerFactory() {
		return (EntityManagerFactory) BeanFactoryUtils.beanOfTypeIncludingAncestors(
				this.beanFactory, EntityManagerFactory.class);
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class AnnotatedMember {

		private final Member member;

		private final String unitName;

		private final PersistenceContextType type;

		private final Properties properties;

		public AnnotatedMember(Member member, String unitName) {
			this(member, unitName, null, null);
		}

		public AnnotatedMember(Member member, String unitName, PersistenceContextType type, Properties properties) {
			this.member = member;
			this.unitName = unitName;
			this.type = type;
			this.properties = properties;

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
				if (!Modifier.isPublic(this.member.getModifiers()) ||
						!Modifier.isPublic(this.member.getDeclaringClass().getModifiers())) {
					((AccessibleObject) this.member).setAccessible(true);
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
			if (this.member instanceof Field) {
				return ((Field) member).getType();
			}
			else if (this.member instanceof Method) {
				Method setter = (Method) this.member;
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
		private Object resolve() {
			// Resolves to EntityManagerFactory or EntityManager.
			if (EntityManagerFactory.class.isAssignableFrom(getMemberType())) {
				EntityManagerFactory emf = resolveEntityManagerFactory();
				if (!getMemberType().isInstance(emf)) {
					throw new IllegalArgumentException("Cannot inject [" + this.member +
							"] with EntityManagerFactory [" + emf + "]: type mismatch");
				}
				return emf;
			}
			else {
				// OK, so we need an EntityManager...
				EntityManager em = (this.type == PersistenceContextType.EXTENDED ?
						resolveExtendedEntityManager() : resolveEntityManager());
				if (!getMemberType().isInstance(em)) {
					throw new IllegalArgumentException("Cannot inject [" + this.member +
							"] with EntityManager [" + em + "]: type mismatch");
				}
				return em;
			}
		}

		private EntityManagerFactory resolveEntityManagerFactory() {
			// Obtain EntityManagerFactory from JNDI?
			EntityManagerFactory emf = getPersistenceUnit(this.unitName);
			if (emf == null) {
				// Need to search for EntityManagerFactory beans.
				emf = findEntityManagerFactory(this.unitName);
			}
			return emf;
		}

		private EntityManager resolveEntityManager() {
			// Obtain EntityManager reference from JNDI?
			EntityManager em = getPersistenceContext(this.unitName, false);
			if (em == null) {
				// No pre-built EntityManager found -> build one based on factory.
				// Obtain EntityManagerFactory from JNDI?
				EntityManagerFactory emf = getPersistenceUnit(this.unitName);
				if (emf == null) {
					// Need to search for EntityManagerFactory beans.
					emf = findEntityManagerFactory(this.unitName);
				}
				// Inject a shared transactional EntityManager proxy.
				if (emf instanceof EntityManagerFactoryInfo &&
						!EntityManager.class.equals(((EntityManagerFactoryInfo) emf).getEntityManagerInterface())) {
					// Create EntityManager based on the info's vendor-specific type
					// (which might be more specific than the field's type).
					em = SharedEntityManagerCreator.createSharedEntityManager(emf, this.properties);
				}
				else {
					// Create EntityManager based on the field's type.
					em = SharedEntityManagerCreator.createSharedEntityManager(emf, this.properties, getMemberType());
				}
			}
			return em;
		}

		private EntityManager resolveExtendedEntityManager() {
			// Obtain EntityManager reference from JNDI?
			EntityManager em = getPersistenceContext(this.unitName, true);
			if (em == null) {
				// No pre-built EntityManager found -> build one based on factory.
				// Obtain EntityManagerFactory from JNDI?
				EntityManagerFactory emf = getPersistenceUnit(this.unitName);
				if (emf == null) {
					// Need to search for EntityManagerFactory beans.
					emf = findEntityManagerFactory(this.unitName);
				}
				// Inject a container-managed extended EntityManager.
				em = ExtendedEntityManagerCreator.createContainerManagedEntityManager(emf, this.properties);
			}
			return em;
		}
	}

}
