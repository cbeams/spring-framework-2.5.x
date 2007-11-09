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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that invokes annotated init and destroy methods. Allows for an annotation
 * alternative to Spring's {@link org.springframework.beans.factory.InitializingBean}
 * and {@link org.springframework.beans.factory.DisposableBean} callback interfaces.
 *
 * <p>The actual annotation types that this post-processor checks for can be
 * configured through the {@link #setInitAnnotationType "initAnnotationType"}
 * and {@link #setDestroyAnnotationType "destroyAnnotationType"} properties.
 * Any custom annotation can be used, since there are no required annotation
 * attributes.
 *
 * <p>Init and destroy annotations may be applied to methods of any visibility:
 * public, package-protected, protected, or private. Multiple such methods
 * may be annotated, but it is recommended to only annotate one single
 * init method and destroy method, respectively.
 *
 * <p>Spring's {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor}
 * supports the JSR-250 {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy}
 * annotations out of the box, as init annotation and destroy annotation, respectively.
 * Furthermore, it also supports the {@link javax.annotation.Resource} annotation
 * for annotation-driven injection of named beans.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #setInitAnnotationType
 * @see #setDestroyAnnotationType
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
public class InitDestroyAnnotationBeanPostProcessor
		implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered, Serializable {

	private Class<? extends Annotation> initAnnotationType;

	private Class<? extends Annotation> destroyAnnotationType;

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	private transient final Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache =
			new ConcurrentHashMap<Class<?>, LifecycleMetadata>();


	/**
	 * Specify the init annotation to check for, indicating initialization
	 * methods to call after configuration of a bean.
	 * <p>Any custom annotation can be used, since there are no required
	 * annotation attributes. There is no default, although a typical choice
	 * is the JSR-250 {@link javax.annotation.PostConstruct} annotation.
	 */
	public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
		this.initAnnotationType = initAnnotationType;
	}

	/**
	 * Specify the destroy annotation to check for, indicating destruction
	 * methods to call when the context is shutting down.
	 * <p>Any custom annotation can be used, since there are no required
	 * annotation attributes. There is no default, although a typical choice
	 * is the JSR-250 {@link javax.annotation.PreDestroy} annotation.
	 */
	public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
		this.destroyAnnotationType = destroyAnnotationType;
	}

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return this.order;
	}


	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class beanType, String beanName) {
		if (beanType != null) {
			LifecycleMetadata metadata = findLifecycleMetadata(beanType);
			for (LifecycleElement lifecycleElement : metadata.getInitMethods()) {
				beanDefinition.registerExternallyManagedInitMethod(lifecycleElement.getMethod().getName());
			}
			for (LifecycleElement lifecycleElement : metadata.getDestroyMethods()) {
				beanDefinition.registerExternallyManagedDestroyMethod(lifecycleElement.getMethod().getName());
			}
		}
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
		try {
			metadata.invokeInitMethods(bean);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Invocation of init method failed", ex);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
		try {
			metadata.invokeDestroyMethods(bean);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Invocation of destroy method failed", ex);
		}
	}


	private LifecycleMetadata findLifecycleMetadata(Class clazz) {
		if (this.lifecycleMetadataCache == null) {
			// Happens after deserialization, during destruction...
			return buildLifecycleMetadata(clazz);
		}
		// Quick check on the concurrent map first, with minimal locking.
		LifecycleMetadata metadata = this.lifecycleMetadataCache.get(clazz);
		if (metadata == null) {
			synchronized (this.lifecycleMetadataCache) {
				metadata = this.lifecycleMetadataCache.get(clazz);
				if (metadata == null) {
					metadata = buildLifecycleMetadata(clazz);
					this.lifecycleMetadataCache.put(clazz, metadata);
				}
				return metadata;
			}
		}
		return metadata;
	}

	private LifecycleMetadata buildLifecycleMetadata(Class clazz) {
		final LifecycleMetadata newMetadata = new LifecycleMetadata();
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				if (initAnnotationType != null) {
					if (method.getAnnotation(initAnnotationType) != null) {
						newMetadata.addInitMethod(method);
					}
				}
				if (destroyAnnotationType != null) {
					if (method.getAnnotation(destroyAnnotationType) != null) {
						newMetadata.addDestroyMethod(method);
					}
				}
			}
		});
		return newMetadata;
	}


	/**
	 * Class representing information about annotated init and destroy methods.
	 */
	private static class LifecycleMetadata {

		private final Set<LifecycleElement> initMethods = new LinkedHashSet<LifecycleElement>();

		private final Set<LifecycleElement> destroyMethods = new LinkedHashSet<LifecycleElement>();

		public void addInitMethod(Method method) {
			this.initMethods.add(new LifecycleElement(method));
		}

		public Set<LifecycleElement> getInitMethods() {
			return this.initMethods;
		}

		public void invokeInitMethods(Object target) throws Throwable {
			for (LifecycleElement lifecycleElement : this.initMethods) {
				lifecycleElement.invoke(target);
			}
		}

		public void addDestroyMethod(Method method) {
			this.destroyMethods.add(new LifecycleElement(method));
		}

		public Set<LifecycleElement> getDestroyMethods() {
			return this.destroyMethods;
		}

		public void invokeDestroyMethods(Object target) throws Throwable {
			for (LifecycleElement lifecycleElement : this.destroyMethods) {
				lifecycleElement.invoke(target);
			}
		}
	}


	/**
	 * Class representing injection information about an annotated method.
	 */
	private static class LifecycleElement {

		private final Method method;

		public LifecycleElement(Method method) {
			if (method.getParameterTypes().length != 0) {
				throw new IllegalStateException("Lifecycle method annotation requires a no-arg method: " + method);
			}
			this.method = method;
		}

		public Method getMethod() {
			return this.method;
		}

		public void invoke(Object target) throws Throwable {
			ReflectionUtils.makeAccessible(this.method);
			try {
				this.method.invoke(target, (Object[]) null);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		public boolean equals(Object other) {
			return (this == other || (other instanceof LifecycleElement &&
					this.method.getName().equals(((LifecycleElement) other).method.getName())));
		}

		public int hashCode() {
			return this.method.getName().hashCode();
		}
	}

}
