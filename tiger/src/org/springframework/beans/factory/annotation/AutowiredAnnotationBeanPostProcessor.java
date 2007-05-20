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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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
 * @author Mark Fisher
 * @since 2.1
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see CommonAnnotationBeanPostProcessor
 */
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements BeanFactoryAware {

	private Class<? extends Annotation> autowiredAnnotationType = Autowired.class;
	
	private String requiredParameterName = "required";
	
	private boolean requiredParameterValue = true;

	private ListableBeanFactory beanFactory;

	private transient final Map<Class<?>, InjectionMetadata> injectionMetadataCache =
			new ConcurrentHashMap<Class<?>, InjectionMetadata>();


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

	/**
	 * Set the name of a parameter of the annotation that specifies
	 * whether it is required.
	 * @see #setRequiredParameterValue(boolean)
	 */
	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	/**
	 * Set the boolean value that marks a dependency as required 
	 * <p>For example if using 'required=true' (the default), 
	 * this value should be <code>true</code>; but if using 
	 * 'optional=false', this value should be <code>false</code>.
	 * @see #setRequiredParameterName(String)
	 */
	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a ListableBeanFactory");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}


	public Constructor[] determineCandidateConstructors(Class beanClass, String beanName) throws BeansException {
		Constructor[] rawCandidates = beanClass.getDeclaredConstructors();
		List candidates = new ArrayList(rawCandidates.length);
		Constructor requiredConstructor = null;
		Constructor defaultConstructor = null;

		for (int i = 0; i < rawCandidates.length; i++) {
			Constructor candidate = rawCandidates[i];
			Annotation annotation = candidate.getAnnotation(getAutowiredAnnotationType());
			if (annotation != null) {
				if (requiredConstructor != null) {
					throw new BeanCreationException("Invalid autowire-marked constructor: " + candidate +
							". Found another constructor with 'required' Autowired annotation: " + requiredConstructor);
				}
				if (candidate.getParameterTypes().length == 0) {
					throw new IllegalStateException("Autowired annotation requires at least one argument: " + candidate);
				}
				boolean required = determineRequiredStatus(annotation);
				if (required) {
					if (!candidates.isEmpty()) {
						throw new BeanCreationException("Invalid autowire-marked constructors: " + candidates +
								". Found another constructor with 'required' Autowired annotation: " + requiredConstructor);
					}
					requiredConstructor = candidate;
				}
				candidates.add(candidate);
			}
			else if (candidate.getParameterTypes().length == 0) {
				defaultConstructor = candidate;
			}
		}

		if (!candidates.isEmpty()) {
			// Add default constructor to list of optional constructors, as fallback.
			if (requiredConstructor == null && defaultConstructor != null) {
				candidates.add(defaultConstructor);
			}
			return (Constructor[]) candidates.toArray(new Constructor[candidates.size()]);
		}
		else {
			// No annotated candidate constructors found.
			return null;
		}
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		InjectionMetadata metadata = findAutowiringMetadata(bean.getClass());
		try {
			metadata.injectFields(bean);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Autowiring of fields failed", ex);
		}
		return true;
	}

	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		InjectionMetadata metadata = findAutowiringMetadata(bean.getClass());
		try {
			metadata.injectMethods(bean, pvs);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Autowiring of methods failed", ex);
		}
		return pvs;
	}


	private InjectionMetadata findAutowiringMetadata(final Class clazz) {
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
		if (metadata == null) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(clazz);
				if (metadata == null) {
					final InjectionMetadata newMetadata = new InjectionMetadata();
					ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) {
							Annotation annotation = field.getAnnotation(getAutowiredAnnotationType());
							if (annotation != null) {
								boolean required = determineRequiredStatus(annotation);
								newMetadata.addInjectedField(new AutowiredElement(field, required, null));
							}
						}
					});
					ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
						public void doWith(Method method) {
							Annotation annotation = method.getAnnotation(getAutowiredAnnotationType());
							if (annotation != null) {
								if (method.getParameterTypes().length == 0) {
									throw new IllegalStateException("Autowired annotation requires at least one argument: " + method);
								}
								boolean required = determineRequiredStatus(annotation);
								PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
								newMetadata.addInjectedMethod(new AutowiredElement(method, required, pd));
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
	 * Obtain a unique bean of the given type.
	 * @param type the type of the bean
	 * @return the target bean or <code>null</code> if no bean of this type is found
	 * @throws BeansException if multiple beans are available
	 */
	protected Object getBeanOfType(Class type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " +
					"override the getBeanOfType method or specify the 'beanFactory' property");
		}
		Map beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
		if (beansOfType.size() > 1) {
			throw new NoSuchBeanDefinitionException(type, "expected single bean but found " + beansOfType.size());
		}
		return beansOfType.size() == 0 ? null : beansOfType.values().iterator().next();
	}

	/**
	 * Determine if the annotated field or method requires its dependency.
	 * <p>A 'required' dependency means that autowiring should fail when no beans
	 * are found. Otherwise, the autowiring process will simply bypass the field
	 * or method when no beans are found.
	 * @param annotation the Autowired annotation
	 * @return whether the annotation indicates that a dependency is required
	 */
	protected boolean determineRequiredStatus(Annotation annotation) {
		try {
			Method method =
					ReflectionUtils.findMethod(annotation.annotationType(), this.requiredParameterName, new Class[0]);
			return (this.requiredParameterValue == (Boolean) ReflectionUtils.invokeMethod(method, annotation));
		}
		catch (Exception ex) {
			// required by default
			return true;
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method.
	 */
	private class AutowiredElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private final PropertyDescriptor pd;

		public AutowiredElement(Member member, boolean required, PropertyDescriptor pd) {
			super(member);
			this.required = required;
			this.pd = pd;
		}

		protected void inject(Object bean, PropertyValues pvs) throws Throwable {
			if (this.member instanceof Field) {
				Field field = (Field) this.member;
				try {
					Object argument = resolveDependency(field.getType());
					if (argument != null) {
						makeMemberAccessible();
						field.set(bean, argument);
					}
				}
				catch (BeansException ex) {
					throw new BeanCreationException("Could not autowire field: " + field, ex);
				}
			}
			else {
				if (this.pd != null && pvs != null && pvs.contains(this.pd.getName())) {
					// Explicit value provided as part of the bean definition.
					return;
				}
				Method method = (Method) this.member;
				Class[] paramTypes = method.getParameterTypes();
				Object[] arguments = new Object[paramTypes.length];
				try {
					boolean shouldInvoke = true; 
					for (int i = 0; i < arguments.length; i++) {
						arguments[i] = resolveDependency(paramTypes[i]);
						if (arguments[i] == null) {
							shouldInvoke = false;
						}
					}
					if (shouldInvoke) {
						makeMemberAccessible();
						method.invoke(bean, arguments);
					}
				}
				catch (BeansException ex) {
					throw new BeanCreationException("Could not autowire method: " + method, ex);
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}
		
		private Object resolveDependency(Class type) {
			Object bean = getBeanOfType(type);
			if (this.required && bean == null) {
				throw new NoSuchBeanDefinitionException(type, "expected single bean but found 0");
			}
			return bean;
		}
	}

}
