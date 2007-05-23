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
import java.util.List;
import java.util.Set;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.AnnotationConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

/**
 * A BeanDefinition scnaner that detects bean candidates on the classpath.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 */
public class ClassPathBeanDefinitionScanner implements ResourceLoaderAware {
	
	private final BeanDefinitionRegistry registry;

	private ResourcePatternResolver resourcePatternResolver;

	private BeanNameGenerator beanNameGenerator = new ComponentBeanNameGenerator();
	
	private ScopeMetadataResolver scopeMetadataResolver;
	
	private boolean includeAnnotationConfig = true;


	/**
	 * A convenience constructor for using the default scopedProxyMode (no proxies).
	 * @param registry the BeanFactory to load bean definitions into,
	 * in the form of a BeanDefinitionRegistry
	 * @see #ClassPathBeanDefinitionScanner(BeanDefinitionRegistry, ScopedProxyMode)
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, ScopedProxyMode.NO);
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
	 * @param scopedProxyMode the proxy behavior for non-singleton scoped beans
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, ScopedProxyMode scopedProxyMode) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
		
		// Determine ResourceLoader to use.
		if (this.registry instanceof ResourceLoader) {
			this.resourcePatternResolver =
					ResourcePatternUtils.getResourcePatternResolver((ResourceLoader) this.registry);
		}
		else {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
		}
	}


	/**
	 * Set the ResourceLoader to use for resource locations.
	 * This will typically be a ResourcePatternResolver implementation.
	 * <p>Default is PathMatchingResourcePatternResolver, also capable of
	 * resource pattern resolving through the ResourcePatternResolver interface.
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * <p>Default is a {@link ComponentBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new ComponentBeanNameGenerator());
	}
	
	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * <p>Default is an {@link AnnotationScopeMetadataResolver}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
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
	 * Perform a scan within the specified base package.
	 * @param basePackage the package to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String basePackage) {
		return scan(new String[] { basePackage });
	}

	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String[] basePackages) {
		return scan(basePackages, true, null, null);
	}

	/**
	 * Perform a scan within the specified base package.
	 * @param basePackage the package to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String basePackage, boolean useDefaultFilters, List<TypeFilter> excludeFilters, List<TypeFilter> includeFilters) {
		return scan(new String[] {basePackage}, useDefaultFilters, excludeFilters, includeFilters);
	}

	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String[] basePackages, boolean useDefaultFilters,
			List<TypeFilter> excludeFilters, List<TypeFilter> includeFilters) {

		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		doScan(basePackages, useDefaultFilters, excludeFilters, includeFilters);

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
	protected Set<BeanDefinitionHolder> doScan(String[] basePackages, boolean useDefaultFilters,
			List<TypeFilter> excludeFilters, List<TypeFilter> includeFilters) {

		// Create the candidate component provider.
		ClassPathScanningCandidateComponentProvider candidateComponentProvider =
				new ClassPathScanningCandidateComponentProvider(basePackages, useDefaultFilters);
		candidateComponentProvider.setResourceLoader(this.resourcePatternResolver);

		if (excludeFilters != null) {
			for (TypeFilter excludeFilter : excludeFilters) {
				candidateComponentProvider.addExcludeFilter(excludeFilter);
			}
		}

		if (includeFilters != null) {
			for (TypeFilter includeFilter : includeFilters) {
				candidateComponentProvider.addIncludeFilter(includeFilter);
			}
		}

		// Find candidate components...
		Set<BeanDefinition> candidates = candidateComponentProvider.findCandidateComponents();
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
		for (BeanDefinition candidate : candidates) {
			String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
			ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
			BeanDefinition beanDefinition = applyScope(candidate, beanName, scopeMetadata);
			beanDefinitions.add(new BeanDefinitionHolder(beanDefinition, beanName));
			this.registry.registerBeanDefinition(beanName, beanDefinition);
		}

		return beanDefinitions;
	}

	private BeanDefinition applyScope(BeanDefinition beanDefinition, String beanName, ScopeMetadata scopeMetadata) {
		beanDefinition.setScope(scopeMetadata.getScopeName());
		ScopedProxyMode scopedProxyMode = scopeMetadata.getScopedProxyMode();
		if (beanDefinition.isSingleton() || scopedProxyMode.equals(ScopedProxyMode.NO)) {
			return beanDefinition;
		}
		boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
		return ScopedProxyCreator.createScopedProxy(definitionHolder, registry, proxyTargetClass);
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
