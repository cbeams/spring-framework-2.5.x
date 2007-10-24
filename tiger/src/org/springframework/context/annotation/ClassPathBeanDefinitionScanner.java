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

package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 * A bean definition scanner that detects bean candidates on the classpath,
 * registering corresponding bean definitions with a given registry (BeanFactory
 * or ApplicationContext).
 *
 * <p>Candidate classes are detected through configurable type filters. The
 * default filters include classes that are annotated with Spring's
 * {@link org.springframework.stereotype.Component @Component},
 * {@link org.springframework.stereotype.Repository @Repository},
 * {@link org.springframework.stereotype.Service @Service}, or
 * {@link org.springframework.stereotype.Controller @Controller} stereotype.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.stereotype.Component
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.stereotype.Controller
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	private final BeanDefinitionRegistry registry;

	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private String[] autowireCandidatePatterns;

	private boolean includeAnnotationConfig = true;


	/**
	 * Create a new ClassPathBeanDefinitionScanner for the given bean factory.
	 * @param registry the BeanFactory to load bean definitions into,
	 * in the form of a BeanDefinitionRegistry
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, true);
	}

	/**
	 * Create a new ClassPathBeanDefinitionScanner for the given bean factory.
	 * <p>If the passed-in bean factory does not only implement the BeanDefinitionRegistry
	 * interface but also the ResourceLoader interface, it will be used as default
	 * ResourceLoader as well. This will usually be the case for
	 * {@link org.springframework.context.ApplicationContext} implementations.
	 * <p>If given a plain BeanDefinitionRegistry, the default ResourceLoader will be a
	 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * @param registry the BeanFactory to load bean definitions into,
	 * in the form of a BeanDefinitionRegistry
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype
	 * annotations.
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		super(useDefaultFilters);

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		if (this.registry instanceof ResourceLoader) {
			setResourceLoader((ResourceLoader) this.registry);
		}
	}


	/**
	 * Set the defaults to use for detected beans.
	 * @see BeanDefinitionDefaults
	 */
	public void setBeanDefinitionDefaults(BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults = (beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}

	/**
	 * Set the name-matching patterns for determining autowire candidates.
	 * @param autowireCandidatePatterns the patterns to match against
	 */
	public void setAutowireCandidatePatterns(String[] autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * <p>Default is a {@link AnnotationBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
	}

	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * Note that this will override any custom "scopedProxyMode" setting.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * @see #setScopedProxyMode
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}

	/**
	 * Specify the proxy behavior for non-singleton scoped beans.
	 * Note that this will override any custom "scopeMetadataResolver" setting.
	 * <p>The default is {@link ScopedProxyMode#NO}.
	 * @see #setScopeMetadataResolver
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}

	/**
	 * Specify whether to register annotation config post-processors.
	 * <p>The default is to register the post-processors. Turn this off
	 * to be able to ignore the annotations or to process them differently.
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String... basePackages) {
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		doScan(basePackages);

		// Register annotation config processors, if necessary.
		if (this.includeAnnotationConfig) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		return this.registry.getBeanDefinitionCount() - beanCountAtScanStart;
	}

	/**
	 * Perform a scan within the specified base packages,
	 * returning the registered bean definitions.
	 * <p>This method does <i>not</i> register an annotation config processor
	 * but rather leaves this up to the caller.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered
	 */
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
		for (int i = 0; i < basePackages.length; i++) {
			Set<BeanDefinition> candidates = findCandidateComponents(basePackages[i]);
			for (BeanDefinition candidate : candidates) {
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
				if (checkBeanName(beanName, candidate)) {
					if (candidate instanceof AbstractBeanDefinition) {
						AbstractBeanDefinition abd = (AbstractBeanDefinition) candidate;
						abd.applyDefaults(this.beanDefinitionDefaults);
						if (this.autowireCandidatePatterns != null) {
							abd.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
						}
					}
					ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
					BeanDefinition beanDefinition = applyScope(candidate, beanName, scopeMetadata);
					beanDefinitions.add(new BeanDefinitionHolder(beanDefinition, beanName));
					this.registry.registerBeanDefinition(beanName, beanDefinition);
				}
			}
		}
		return beanDefinitions;
	}


	/**
	 * Check the given bean name, determining whether the corresponding
	 * bean definition needs to be registered or conflicts with an
	 * existing definition.
	 * @param beanName the suggested name for the bean
	 * @param beanDefinition the corresponding bean definition
	 * @return <code>true</code> if the bean can be registered as-is;
	 * <code>false</code> if it should be skipped because there is an
	 * existing, compatible bean definition for the specified name
	 * @throws IllegalStateException if an existing, incompatible
	 * bean definition has been found for the specified name
	 */
	private boolean checkBeanName(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}
		throw new IllegalStateException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * Determine whether the given new bean definition is compatible with
	 * the given existing bean definition.
	 * <p>The default implementation simply considers them as compatible
	 * when the bean class name matches.
	 * @param newDefinition the new bean definition, originated from scanning
	 * @param existingDefinition the existing bean definition, probably from
	 * an existing bean definition
	 * @return whether the definitions are considered as compatible, with the
	 * new definition to be skipped in favor of the existing definition
	 */
	private boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
		return newDefinition.getBeanClassName().equals(existingDefinition.getBeanClassName());
	}

	/**
	 * Apply the specified scope to the given bean definition.
	 * @param beanDefinition the bean definition to configure
	 * @param beanName the name of the bean
	 * @param scopeMetadata the corresponding scope metadata
	 * @return the final bean definition to use (potentially a proxy)
	 */
	private BeanDefinition applyScope(BeanDefinition beanDefinition, String beanName, ScopeMetadata scopeMetadata) {
		String scope = scopeMetadata.getScopeName();
		ScopedProxyMode scopedProxyMode = scopeMetadata.getScopedProxyMode();
		beanDefinition.setScope(scope);
		if (BeanDefinition.SCOPE_SINGLETON.equals(scope) || BeanDefinition.SCOPE_PROTOTYPE.equals(scope) ||
				scopedProxyMode.equals(ScopedProxyMode.NO)) {
			return beanDefinition;
		}
		boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
		return ScopedProxyCreator.createScopedProxy(definitionHolder, this.registry, proxyTargetClass);
	}


	/**
	 * Inner factory class used to just introduce an AOP framework dependency
	 * when actually creating a scoped proxy.
	 */
	private static class ScopedProxyCreator {

		public static BeanDefinition createScopedProxy(
				BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry, boolean proxyTargetClass) {

			BeanDefinitionHolder scopedProxyDefinition =
					ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
			return scopedProxyDefinition.getBeanDefinition();
		}
	}

}
