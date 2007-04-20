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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
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
 * <p>The common JSR-250 annotations supported by this post-processor are
 * available in Java 6 (JDK 1.6) as well as in Java EE 5 (which provides
 * a standalone jar for its common annotations as well, allowing for use
 * in any Java 5 based application). Hence, this post-processor works
 * out of the box on JDK 1.6, and requires the JSR-250 API jar to be added
 * on JDK 1.5.
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
 * @author Juergen Hoeller
 * @since 2.1
 * @see #setResourceFactory
 * @see InitDestroyAnnotationBeanPostProcessor
 */
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

	private transient BeanFactory resourceFactory;

	private transient final Map<Class<?>, ResourceMetadata> resourceMetadataCache =
			new HashMap<Class<?>, ResourceMetadata>();


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
		ResourceMetadata metadata = findResourceMetadata(bean.getClass());
		try {
			metadata.injectResources(bean);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resources failed", ex);
		}
		return true;
	}

	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}


	private ResourceMetadata findResourceMetadata(Class clazz) {
		synchronized (this.resourceMetadataCache) {
			ResourceMetadata metadata = this.resourceMetadataCache.get(clazz);
			if (metadata == null) {
				final ResourceMetadata newMetadata = new ResourceMetadata();
				ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
					public void doWith(Field field) {
						if (field.getAnnotation(Resource.class) != null) {
							newMetadata.addResource(field);
						}
					}
				});
				ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
					public void doWith(Method method) {
						if (method.getAnnotation(Resource.class) != null) {
							if (method.getParameterTypes().length != 1) {
								throw new IllegalStateException("Resource annotation requires a single-arg method: " + method);
							}
							newMetadata.addResource(method);
						}
					}
				});
				metadata = newMetadata;
				this.resourceMetadataCache.put(clazz, metadata);
			}
			return metadata;
		}
	}

	protected Object getResource(String name, Class type, boolean shareable) throws BeansException {
		if (this.resourceFactory == null) {
			throw new IllegalStateException("No resource factory configured - " +
					"override the getResource method or specify the 'resourceFactory' property");
		}
		return this.resourceFactory.getBean(name, type);
	}


	/**
	 * Class representing information about annotated fields and methods.
	 */
	private class ResourceMetadata {

		private Set<ResourceElement> resourceElements = new LinkedHashSet<ResourceElement>();

		public void addResource(Member member) {
			this.resourceElements.add(new ResourceElement(member));
		}

		public void injectResources(Object target) throws Throwable {
			for (Iterator it = this.resourceElements.iterator(); it.hasNext();) {
				ResourceElement element = (ResourceElement) it.next();
				element.injectResource(target);
			}
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class ResourceElement {

		private final Member member;

		private final String name;

		private final Class type;

		private final boolean shareable;

		public ResourceElement(Member member) {
			AnnotatedElement ae = (AnnotatedElement) member;
			Resource resource = ae.getAnnotation(Resource.class);
			String resourceName = resource.name();
			Class resourceType = resource.type();
			if (!StringUtils.hasLength(resourceName)) {
				resourceName = member.getName();
				if (member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			if (resourceType != null && !Object.class.equals(resourceType)) {
				if (member instanceof Field) {
					Class fieldType = ((Field) member).getType();
					if (!fieldType.isAssignableFrom(resourceType)) {
						throw new IllegalStateException("Specified resource type [" + resourceType.getName() +
								"] is not assignable to field type [" + fieldType + "]");
					}
				}
				else {
					Class paramType = ((Method) member).getParameterTypes()[0];
					if (!paramType.isAssignableFrom(resourceType)) {
						throw new IllegalStateException("Specified resource type [" + resourceType.getName() +
								"] is not assignable to method parameter type [" + paramType + "]");
					}
				}
			}
			else {
				// No resource type specified...
				if (member instanceof Field) {
					resourceType = ((Field) member).getType();
				}
				else {
					resourceType = ((Method) member).getParameterTypes()[0];
				}
			}
			this.member = member;
			this.name = resourceName;
			this.type = resourceType;
			this.shareable = resource.shareable();
		}

		public void injectResource(Object bean) throws Throwable {
			Object resourceObject = getResource(this.name, this.type, this.shareable);
			if (!Modifier.isPublic(this.member.getModifiers()) ||
					!Modifier.isPublic(this.member.getDeclaringClass().getModifiers())) {
				((AccessibleObject) this.member).setAccessible(true);
			}
			if (this.member instanceof Field) {
				((Field) this.member).set(bean, resourceObject);
			}
			else {
				try {
					((Method) this.member).invoke(bean, resourceObject);
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ResourceElement)) {
				return false;
			}
			ResourceElement otherElement = (ResourceElement) other;
			if (this.member instanceof Field) {
				return this.member.equals(otherElement.member);
			}
			else {
				return (otherElement.member instanceof Method &&
						this.member.getName().equals(otherElement.member.getName()) &&
						Arrays.equals(((Method) this.member).getParameterTypes(),
								((Method) otherElement.member).getParameterTypes()));
			}
		}

		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}
	}

}
