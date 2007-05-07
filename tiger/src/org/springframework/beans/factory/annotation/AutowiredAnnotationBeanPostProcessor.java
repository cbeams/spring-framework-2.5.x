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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that autowires annotated fields, setter methods and arbitrary config methods.
 * Such members to be injected are detected through a Java 5 annotation:
 * by default, Spring's {@link Autowired} annotation.
 *
 * <p>Only one constructor (at max) of any given bean class may carry this
 * annotation, indicating the constructor to autowire when used as a Spring
 * bean. Such a constructor does not have to be public.
 *
 * <p>Fields are injected right after construction of a bean, before any
 * config methods are invoked. Such a config field does not have to be public.
 *
 * <p>Config methods may have an arbitrary name and any number of arguments;
 * each of those arguments will be autowired with a matching bean in the
 * Spring container. Bean property setter methods are effectively just
 * a special case of such a general config method. Such config methods
 * do not have to be public.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see CommonAnnotationBeanPostProcessor
 */
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements BeanFactoryAware {

	private transient final Map<Class<?>, AutowiringMetadata> memberMetadataCache =
			new HashMap<Class<?>, AutowiringMetadata>();

	private ListableBeanFactory beanFactory;


	private Class<? extends Annotation> autowiredAnnotationType = Autowired.class;


	/**
	 * Set the 'autowired' annotation type, to be used on constructors, fields,
	 * setter methods and arbitrary config methods.
	 * <p>The default autowired annotation type is the Spring-provided
	 * {@link Autowired} annotation.
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a member is
	 * supposed to be autowired.
	 */
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationType = autowiredAnnotationType;
	}

	/**
	 * Return the 'autowired' annotation type.
	 */
	protected Class<? extends Annotation> getAutowiredAnnotationType() {
		return this.autowiredAnnotationType;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a ListableBeanFactory");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}


	public Constructor determineConstructor(Class beanClass, String beanName) throws BeansException {
		Constructor[] candidates = beanClass.getDeclaredConstructors();
		Constructor constructor = null;
		for (int i = 0; i < candidates.length; i++) {
			Constructor candidate = candidates[i];
			if (candidate.getAnnotation(getAutowiredAnnotationType()) != null) {
				if (constructor != null) {
					throw new BeanCreationException(
							"Multiple constructors carrying the Autowired annotation: " + constructor + ", " + candidate);
				}
				constructor = candidate;
			}
		}
		return constructor;
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		AutowiringMetadata metadata = findAutowiringMetadata(bean.getClass());
		try {
			metadata.autowireMembers(bean);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resources failed", ex);
		}
		return true;
	}


	private AutowiringMetadata findAutowiringMetadata(Class clazz) {
		synchronized (this.memberMetadataCache) {
			AutowiringMetadata metadata = this.memberMetadataCache.get(clazz);
			if (metadata == null) {
				final AutowiringMetadata newMetadata = new AutowiringMetadata();
				ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
					public void doWith(Field field) {
						if (field.getAnnotation(getAutowiredAnnotationType()) != null) {
							newMetadata.addAutowiredMember(field);
						}
					}
				});
				ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
					public void doWith(Method method) {
						if (method.getAnnotation(getAutowiredAnnotationType()) != null) {
							if (method.getParameterTypes().length == 0) {
								throw new IllegalStateException("Autowired annotation requires at least one argument: " + method);
							}
							newMetadata.addAutowiredMember(method);
						}
					}
				});
				metadata = newMetadata;
				this.memberMetadataCache.put(clazz, metadata);
			}
			return metadata;
		}
	}

	/**
	 * Obtain a unique bean of the given type.
	 * @param type the type of the bean
	 * @return the target bean (never <code>null</code>)
	 * @throws BeansException if we failed to obtain the bean
	 */
	protected Object getBeanOfType(Class type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " +
					"override the getBeanOfType method or specify the 'beanFactory' property");
		}
		return BeanFactoryUtils.beanOfTypeIncludingAncestors(this.beanFactory, type);
	}


	/**
	 * Class representing information about annotated fields and methods.
	 */
	private class AutowiringMetadata {

		private Set<AutowiredMember> autowiredMembers = new LinkedHashSet<AutowiredMember>();

		public void addAutowiredMember(Member member) {
			this.autowiredMembers.add(new AutowiredMember(member));
		}

		public void autowireMembers(Object target) throws Throwable {
			for (Iterator it = this.autowiredMembers.iterator(); it.hasNext();) {
				AutowiredMember element = (AutowiredMember) it.next();
				element.autowireMember(target);
			}
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class AutowiredMember {

		private final Member member;

		public AutowiredMember(Member member) {
			this.member = member;
		}

		public void autowireMember(Object bean) throws Throwable {
			if (!Modifier.isPublic(this.member.getModifiers()) ||
					!Modifier.isPublic(this.member.getDeclaringClass().getModifiers())) {
				((AccessibleObject) this.member).setAccessible(true);
			}
			if (this.member instanceof Field) {
				Field field = (Field) this.member;
				try {
					Object argument = getBeanOfType(field.getType());
					field.set(bean, argument);
				}
				catch (BeansException ex) {
					throw new BeanCreationException("Could not autowire field: " + field, ex);
				}
			}
			else {
				Method method = (Method) this.member;
				Class[] paramTypes = method.getParameterTypes();
				Object[] arguments = new Object[paramTypes.length];
				try {
					for (int i = 0; i < arguments.length; i++) {
						arguments[i] = getBeanOfType(paramTypes[i]);
					}
					method.invoke(bean, arguments);
				}
				catch (BeansException ex) {
					throw new BeanCreationException("Could not autowire method: " + method, ex);
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
			if (!(other instanceof AutowiredMember)) {
				return false;
			}
			AutowiredMember otherElement = (AutowiredMember) other;
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
