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

package org.springframework.beans.factory.annotation;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that supports common JSR-250 annotations out of the box.
 *
 * <p>This includes support for the {@link javax.annotation.PostConstruct} and
 * {@link javax.annotation.PreDestroy} annotations (as init annotation and destroy
 * annotation, respectively). Also supports the {@link javax.annotation.Resource}
 * annotation for annotation-driven injection of named beans, either from the
 * containing BeanFactory or from a specified resource factory
 * (e.g. Spring's {@link org.springframework.jndi.support.SimpleJndiBeanFactory} for
 * JNDI lookup behavior equivalent to standard Java EE 5 resource injection}.
 *
 * <p>The common JSR-250 annotations supported by this post-processor are available
 * in Java 6 (JDK 1.6) as well as in Java EE 5 (which provides a standalone jar for
 * its common annotations as well, allowing for use in any Java 5 based application).
 * Hence, this post-processor works out of the box on JDK 1.6, and requires the
 * JSR-250 API jar to be added to the classpath on JDK 1.5.
 *
 * <p>For default usage, resolving resource names as Spring bean names,
 * simply define the following in your application context:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor"/&gt;</pre>
 *
 * For direct JNDI access, resolving resource names as JNDI resource references
 * within the J2EE application's "java:comp/env/" namespace, use the following:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor"&gt;
 *   &lt;property name="resourceFactory"&gt;
 *     &lt;bean class="org.springframework.jndi.support.SimpleJndiBeanFactory"/&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Note: A default CommonAnnotationBeanPostProcessor will be registered
 * by the "context:annotation-config" and "context:component-scan" XML tags.
 * Remove or turn off the default annotation configuration there if you intend
 * to specify a custom CommonAnnotationBeanPostProcessor bean definition.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see #setResourceFactory
 * @see InitDestroyAnnotationBeanPostProcessor
 * @see AutowiredAnnotationBeanPostProcessor
 */
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

	private boolean fallbackToDefaultTypeMatch = true;

	private transient BeanFactory resourceFactory;

	private transient final Map<Class<?>, InjectionMetadata> injectionMetadataCache =
			new ConcurrentHashMap<Class<?>, InjectionMetadata>();


	/**
	 * Create a new CommonAnnotationBeanPostProcessor,
	 * with the init and destroy annotation types set to
	 * {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy},
	 * respectively.
	 */
	public CommonAnnotationBeanPostProcessor() {
		setInitAnnotationType(PostConstruct.class);
		setDestroyAnnotationType(PreDestroy.class);
	}


	/**
	 * Set whether to allow a fallback to a type match if no explicit name has been
	 * specified. The default name (i.e. the field name or bean property name) will
	 * still be checked first; if a bean of that name exists, it will be taken.
	 * However, if no bean of that name exists, a by-type resolution of the
	 * dependency will be attempted if this flag is "true".
	 * <p>Default is "true". Switch this flag to "false" in order to enforce a
	 * by-name lookup in all cases, throwing an exception in case of no name match.
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency
	 */
	public void setFallbackToDefaultTypeMatch(boolean fallbackToDefaultTypeMatch) {
		this.fallbackToDefaultTypeMatch = fallbackToDefaultTypeMatch;
	}

	/**
	 * Specify the factory for objects to be injected into <code>@Resource</code>
	 * annotated fields and setter methods.
	 * <p>The default is the BeanFactory that this post-processor is defined in,
	 * if any, looking up resource names as Spring bean names. Specify the resource
	 * factory explicitly for programmatic usage of this post-processor.
	 * <p>Specify Spring's {@link org.springframework.jndi.support.SimpleJndiBeanFactory}
	 * for JNDI lookup behavior equivalent to standard Java EE 5 resource injection,
	 * as available to Servlet 2.5 Servlets and EJB 3.0 Session Beans.
	 * @param resourceFactory the factory in the form of a Spring BeanFactory
	 */
	public void setResourceFactory(BeanFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (this.resourceFactory == null) {
			this.resourceFactory = beanFactory;
		}
	}


	public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
		return null;
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		InjectionMetadata metadata = findResourceMetadata(bean.getClass());
		try {
			metadata.injectFields(bean, beanName);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resource fields failed", ex);
		}
		return true;
	}

	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		InjectionMetadata metadata = findResourceMetadata(bean.getClass());
		try {
			metadata.injectMethods(bean, beanName, pvs);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resource methods failed", ex);
		}
		return pvs;
	}


	private InjectionMetadata findResourceMetadata(final Class clazz) {
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
		if (metadata == null) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(clazz);
				if (metadata == null) {
					final InjectionMetadata newMetadata = new InjectionMetadata();
					ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) {
							if (field.getAnnotation(Resource.class) != null) {
								if (Modifier.isStatic(field.getModifiers())) {
									throw new IllegalStateException("Resource annotation is not supported on static fields");
								}
								newMetadata.addInjectedField(new ResourceElement(field, null));
							}
						}
					});
					ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
						public void doWith(Method method) {
							if (method.getAnnotation(Resource.class) != null) {
								if (Modifier.isStatic(method.getModifiers())) {
									throw new IllegalStateException("Resource annotation is not supported on static methods");
								}
								if (method.getParameterTypes().length != 1) {
									throw new IllegalStateException("Resource annotation requires a single-arg method: " + method);
								}
								PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
								newMetadata.addInjectedMethod(new ResourceElement(method, pd));
							}
						}
					});
					metadata = newMetadata;
					this.injectionMetadataCache.put(clazz, metadata);
				}
			}
		}
		return metadata;
	}

	/**
	 * Obtain the resource object for the given name and type.
	 * @param resourceElement the descriptor for the annotated field/method
	 * @param requestingBeanName the name of the requesting bean
	 * @return the resource object (never <code>null</code>)
	 * @throws BeansException if we failed to obtain the target resource
	 */
	protected Object getResource(ResourceElement resourceElement, String requestingBeanName)
			throws BeansException {

		if (this.resourceFactory == null) {
			throw new IllegalStateException("No resource factory configured - " +
					"override the getResource method or specify the 'resourceFactory' property");
		}

		Object resource = null;
		Set autowiredBeanNames = null;
		String name = resourceElement.getName();
		Class type = resourceElement.getType();

		if (this.fallbackToDefaultTypeMatch && this.resourceFactory instanceof AutowireCapableBeanFactory &&
				resourceElement.isDefaultName() && !this.resourceFactory.containsBean(name)) {
			autowiredBeanNames = new HashSet();
			resource = ((AutowireCapableBeanFactory) this.resourceFactory).resolveDependency(
					resourceElement.getDependencyDescriptor(), requestingBeanName, autowiredBeanNames, null);
		}
		else {
			resource = this.resourceFactory.getBean(name, type);
			autowiredBeanNames = Collections.singleton(name);
		}

		if (this.resourceFactory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) this.resourceFactory;
			for (Iterator it = autowiredBeanNames.iterator(); it.hasNext();) {
				String autowiredBeanName = (String) it.next();
				beanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
			}
		}

		return resource;
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	protected class ResourceElement extends InjectionMetadata.InjectedElement {

		private final String name;

		private final boolean isDefaultName;

		private final Class type;

		private final boolean shareable;

		public ResourceElement(Member member, PropertyDescriptor pd) {
			super(member, pd);
			AnnotatedElement ae = (AnnotatedElement) member;
			Resource resource = ae.getAnnotation(Resource.class);
			String resourceName = resource.name();
			Class resourceType = resource.type();
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				resourceName = member.getName();
				if (member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			if (resourceType != null && !Object.class.equals(resourceType)) {
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method.
				resourceType = getResourceType();
			}
			this.name = resourceName;
			this.type = resourceType;
			this.shareable = resource.shareable();
		}

		/**
		 * Return the name of the target resource.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Return whether the given name is a default name
		 * (derived from the field name / method name)
		 */
		public boolean isDefaultName() {
			return this.isDefaultName;
		}

		/**
		 * Return the type of the target resource
		 */
		public Class getType() {
			return this.type;
		}

		/**
		 * Return whether the target resource is shareable
		 * (as defined in the @Resource annotation)
		 */
		public boolean isShareable() {
			return this.shareable;
		}

		/**
		 * Build a DependencyDescriptor for the underlying field/method.
		 */
		public DependencyDescriptor getDependencyDescriptor() {
			if (this.isField) {
				return new ResourceDependencyDescriptor((Field) this.member, this.type);
			}
			else {
				return new ResourceDependencyDescriptor((Method) this.member, this.type);
			}
		}

		@Override
		protected Object getResourceToInject(Object target, String requestingBeanName) {
			return getResource(this, requestingBeanName);
		}
	}


	/**
	 * Extension of the DependencyDescriptor class,
	 * overriding the dependency type with the specified resource type.
	 */
	private static class ResourceDependencyDescriptor extends DependencyDescriptor {

		private final Class resourceType;

		public ResourceDependencyDescriptor(Field field, Class resourceType) {
			super(field, true);
			this.resourceType = resourceType;
		}

		public ResourceDependencyDescriptor(Method method, Class resourceType) {
			super(new MethodParameter(method, 0), true);
			this.resourceType = resourceType;
		}

		public Class getDependencyType() {
			return this.resourceType;
		}
	}

}
