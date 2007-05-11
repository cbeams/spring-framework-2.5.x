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

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.typefilter.TypeFilter;
import org.springframework.util.Assert;

/**
 * A {@link BeanDefinitionScanner} that detects bean candidates on the classpath. 
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 */
public class ClassPathBeanDefinitionScanner extends AnnotationConfigRegistrar 
		implements BeanDefinitionScanner, ResourceLoaderAware {

	private final BeanDefinitionRegistry registry;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	private BeanNameGenerator beanNameGenerator = new ComponentBeanNameGenerator();
	
	boolean includeAnnotationConfig = true;
	
	
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;
	}
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}
	
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}
	
	/**
	 * Specify whether to register annotation config post processors.
	 * <p>enabled by default 
	 * 
	 * @param includeAnnotationConfig
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}

	/**
	 * Perform a scan within the specified base package.
	 * 
	 * @param basePackage
	 * @return number of beans registered
	 */
	public int scan(String basePackage) {
		return this.scan(new String[] { basePackage });
	}

	/**
	 * Perform a scan within the specified base packages.
	 * 
	 * @param basePackages
	 * @return number of beans registered
	 */
	public int scan(String[] basePackages) {
		return this.scan(basePackages, true, null, null);
	}

	/**
	 * Perform a scan within the specified base package.
	 * 
	 * @param basePackage
	 * @param useDefaultFilters
	 * @param excludeFilters
	 * @param includeFilters
	 * @return number of beans registered
	 */
	public int scan(String basePackage, boolean useDefaultFilters, List<TypeFilter> excludeFilters, List<TypeFilter> includeFilters) {
		return this.scan(new String[] { basePackage }, useDefaultFilters, excludeFilters, includeFilters);
	}
		
	/**
	 * Perform a scan within the specified base packages.
	 * 
	 * @param basePackages
	 * @param useDefaultFilters
	 * @param excludeFilters
	 * @param includeFilters
	 * @return number of beans registered
	 */
	public int scan(String[] basePackages, boolean useDefaultFilters, List<TypeFilter> excludeFilters, List<TypeFilter> includeFilters) {
		
		int beanCountAtScanStart = registry.getBeanDefinitionCount();
		
		// create the component provider
		ClassPathScanningCandidateComponentProvider candidateComponentProvider =
				new ClassPathScanningCandidateComponentProvider(basePackages, useDefaultFilters);
		candidateComponentProvider.setResourceLoader(resourcePatternResolver);
		
		if (excludeFilters != null) {
			for(TypeFilter excludeFilter : excludeFilters) {
				candidateComponentProvider.addExcludeFilter(excludeFilter);
			}
		}
		
		if (includeFilters != null) {
			for(TypeFilter includeFilter : includeFilters) {
				candidateComponentProvider.addIncludeFilter(includeFilter);
			}
		}
		
		// find candidate components
		Set<Class> candidates = candidateComponentProvider.findCandidateComponents();		

		// register base bean definitions
		for (Class<?> beanClass : candidates) {
			BeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
			String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
			registry.registerBeanDefinition(beanName, beanDefinition);
		}
		
		// register annotation config post processors if necessary
		if (includeAnnotationConfig) {
			registerAnnotationConfigProcessors(registry);
		}
		
		return registry.getBeanDefinitionCount() - beanCountAtScanStart;
	}

}
