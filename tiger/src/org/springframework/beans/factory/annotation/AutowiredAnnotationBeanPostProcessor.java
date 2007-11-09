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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that autowires annotated fields, setter methods and arbitrary config methods.
 * Such members to be injected are detected through a Java 5 annotation:
 * by default, Spring's {@link Autowired} annotation.
 *
 * <p>Only one constructor (at max) of any given bean class may carry this
 * annotation with the 'required' parameter set to <code>true</code>, 
 * indicating <i>the</i> constructor to autowire when used as a Spring bean. 
 * If multiple <i>non-required</i> constructors carry the annotation, they 
 * will be considered as candidates for autowiring. The constructor with 
 * the greatest number of dependencies that can be satisfied by matching
 * beans in the Spring container will be chosen. If none of the candidates
 * can be satisfied, then a default constructor (if present) will be used.
 * An annotated constructor does not have to be public.
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
 * <p>Note: A default AutowiredAnnotationBeanPostProcessor will be registered
 * by the "context:annotation-config" and "context:component-scan" XML tags.
 * Remove or turn off the default annotation configuration there if you intend
 * to specify a custom AutowiredAnnotationBeanPostProcessor bean definition.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements PriorityOrdered, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(AutowiredAnnotationBeanPostProcessor.class);

	private Class<? extends Annotation> autowiredAnnotationType = Autowired.class;
	
	private String requiredParameterName = "required";
	
	private boolean requiredParameterValue = true;

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	private ConfigurableListableBeanFactory beanFactory;

	private final Map<Class<?>, Constructor[]> candidateConstructorsCache =
			new ConcurrentHashMap<Class<?>, Constructor[]>();

	private final Map<Class<?>, InjectionMetadata> injectionMetadataCache =
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

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return this.order;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}


	public Constructor[] determineCandidateConstructors(Class beanClass, String beanName) throws BeansException {
		// Quick check on the concurrent map first, with minimal locking.
		Constructor[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					Constructor[] rawCandidates = beanClass.getDeclaredConstructors();
					List<Constructor> candidates = new ArrayList<Constructor>(rawCandidates.length);
					Constructor requiredConstructor = null;
					Constructor defaultConstructor = null;
					for (int i = 0; i < rawCandidates.length; i++) {
						Constructor<?> candidate = rawCandidates[i];
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
						candidateConstructors = (Constructor[]) candidates.toArray(new Constructor[candidates.size()]);
					}
					else {
						candidateConstructors = new Constructor[0];
					}
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return (candidateConstructors.length > 0 ? candidateConstructors : null);
	}

	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		InjectionMetadata metadata = findAutowiringMetadata(bean.getClass());
		try {
			metadata.injectFields(bean, beanName);
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
			metadata.injectMethods(bean, beanName, pvs);
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
								if (Modifier.isStatic(field.getModifiers())) {
									throw new IllegalStateException("Autowired annotation is not supported on static fields");
								}
								boolean required = determineRequiredStatus(annotation);
								newMetadata.addInjectedField(new AutowiredElement(field, required, null));
							}
						}
					});
					ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
						public void doWith(Method method) {
							Annotation annotation = method.getAnnotation(getAutowiredAnnotationType());
							if (annotation != null) {
								if (Modifier.isStatic(method.getModifiers())) {
									throw new IllegalStateException("Autowired annotation is not supported on static methods");
								}
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
	 * Obtain all beans of the given type as autowire candidates.
	 * @param type the type of the bean
	 * @return the target beans, or an empty Collection if no bean of this type is found
	 * @throws BeansException if bean retrieval failed
	 */
	protected Map findAutowireCandidates(Class type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " +
					"override the getBeanOfType method or specify the 'beanFactory' property");
		}
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
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
			Method method = ReflectionUtils.findMethod(annotation.annotationType(), this.requiredParameterName);
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

		private volatile String beanNameForField;

		private volatile String[] beanNamesForMethod;

		public AutowiredElement(Member member, boolean required, PropertyDescriptor pd) {
			super(member, pd);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			if (this.skip) {
				return;
			}
			if (this.isField) {
				Field field = (Field) this.member;
				try {
					Object argument = null;
					String determinedBeanName = this.beanNameForField;
					if (determinedBeanName != null) {
						argument = beanFactory.getBean(determinedBeanName);
					}
					else {
						Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
						TypeConverter typeConverter = beanFactory.getTypeConverter();
						argument = beanFactory.resolveDependency(
								new DependencyDescriptor(field, this.required),
								beanName, autowiredBeanNames, typeConverter);
						registerDependentBeans(beanName, autowiredBeanNames);
						if (autowiredBeanNames.size() == 1) {
							this.beanNameForField = autowiredBeanNames.iterator().next();
						}
					}
					if (argument != null) {
						ReflectionUtils.makeAccessible(field);
						field.set(bean, argument);
					}
				}
				catch (Throwable ex) {
					throw new BeanCreationException("Could not autowire field: " + field, ex);
				}
			}
			else {
				if (this.pd != null && pvs != null && pvs.contains(this.pd.getName())) {
					// Explicit value provided as part of the bean definition.
					this.skip = true;
					return;
				}
				Method method = (Method) this.member;
				Object[] arguments = new Object[method.getParameterTypes().length];
				try {
					String[] determinedBeanNames = this.beanNamesForMethod;
					if (determinedBeanNames != null) {
						for (int i = 0; i < determinedBeanNames.length; i++) {
							arguments[i] = beanFactory.getBean(determinedBeanNames[i]);
						}
					}
					else {
						Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
						TypeConverter typeConverter = beanFactory.getTypeConverter();
						for (int i = 0; i < arguments.length; i++) {
							arguments[i] = beanFactory.resolveDependency(
									new DependencyDescriptor(new MethodParameter(method, i), this.required),
									beanName, autowiredBeanNames, typeConverter);
							if (arguments[i] == null) {
								arguments = null;
								break;
							}
						}
						if (arguments != null) {
							registerDependentBeans(beanName, autowiredBeanNames);
							if (autowiredBeanNames.size() == arguments.length) {
								this.beanNamesForMethod = autowiredBeanNames.toArray(new String[arguments.length]);
							}
						}
					}
					if (arguments != null) {
						ReflectionUtils.makeAccessible(method);
						method.invoke(bean, arguments);
					}
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
				catch (Throwable ex) {
					throw new BeanCreationException("Could not autowire method: " + method, ex);
				}
			}
		}

		private void registerDependentBeans(String beanName, Set<String> autowiredBeanNames) {
			for (Iterator it = autowiredBeanNames.iterator(); it.hasNext();) {
				String autowiredBeanName = (String) it.next();
				beanFactory.registerDependentBean(autowiredBeanName, beanName);
				if (logger.isDebugEnabled()) {
					logger.debug("Autowiring by type from bean name '" + beanName + "' via " +
							(this.isField ? "field" : "configuration method") + " to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}

}
