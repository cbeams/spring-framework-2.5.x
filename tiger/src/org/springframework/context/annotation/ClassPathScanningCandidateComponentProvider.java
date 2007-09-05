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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.asm.CachingClassReaderFactory;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A component provider that scans the classpath from a base package.
 * It then applies exclude and include filters to the resulting classes to find candidates.
 *
 * <p>This implementation is based on the ASM {@link org.objectweb.asm.ClassReader}.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @since 2.1
 * @see org.objectweb.asm.ClassReader
 * @see org.springframework.core.type.asm.AnnotationMetadataReadingVisitor
 */
public class ClassPathScanningCandidateComponentProvider implements ResourceLoaderAware {

	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private ClassReaderFactory classReaderFactory = new CachingClassReaderFactory(this.resourcePatternResolver);

	private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

	private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();


	/**
	 * Create a ClassPathScanningCandidateComponentProvider.
	 * @param useDefaultFilters whether to register the default filters for the
	 * <code>@Component</code> and <code>@Repository</code> annotations
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
		if (useDefaultFilters) {
			registerDefaultFilters();
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
		this.classReaderFactory = new CachingClassReaderFactory(resourceLoader);
	}

	/**
	 * Set the resource pattern to use when scanning the classpath.
	 * This value will be appended to each base package name.
	 * @see #findCandidateComponents(String)
	 * @see #DEFAULT_RESOURCE_PATTERN
	 */
	public void setResourcePattern(String resourcePattern) {
		Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
		this.resourcePattern = resourcePattern;
	}

	/**
	 * Add an include type filter to the <i>end</i> of the inclusion list.
	 */
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	/**
	 * Add an exclude type filter to the <i>front</i> of the exclusion list.
	 */
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	/**
	 * Reset the configured type filters.
	 * @param useDefaultFilters whether to re-register the default filters for the
	 * <code>@Component</code> and <code>@Repository</code> annotations
	 * @see #registerDefaultFilters()
	 */
	public void resetFilters(boolean useDefaultFilters) {
		this.includeFilters.clear();
		this.excludeFilters.clear();
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
	}

	/**
	 * Register the default filters for the <code>@Component</code>
	 * and <code>@Repository</code> annotations.
	 * @see org.springframework.stereotype.Component
	 * @see org.springframework.stereotype.Repository
	 */
	protected void registerDefaultFilters() {
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		this.includeFilters.add(new AnnotationTypeFilter(Repository.class));
	}


	/**
	 * Scan the class path for candidate components.
	 * @param basePackage the package to check for annotated classes
	 * @return a corresponding Set of autodetected bean definitions
	 */
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
		try {
			String packageSearchPath =
					"classpath*:" + ClassUtils.convertClassNameToResourcePath(basePackage) + "/" + this.resourcePattern;
			Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
			for (int i = 0; i < resources.length; i++) {
				Resource resource = resources[i];
				ClassReader classReader = this.classReaderFactory.getClassReader(resource);
				if (isCandidateComponent(classReader)) {
					ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(classReader);
					sbd.setSource(resource);
					if (isCandidateComponent(sbd)) {
						candidates.add(sbd);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}

	/**
	 * Determine whether the given class does not match any exclude filter
	 * and does match at least one include filter.
	 * @param classReader the ASM ClassReader for the class
	 * @return whether the class qualifies as a candidate component
	 */
	protected boolean isCandidateComponent(ClassReader classReader) throws IOException {
		for (TypeFilter tf : this.excludeFilters) {
			if (tf.match(classReader, this.classReaderFactory)) {
				return false;
			}
		}
		for (TypeFilter tf : this.includeFilters) {
			if (tf.match(classReader, this.classReaderFactory)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether the given bean definition qualifies as candidate.
	 * <p>The default implementation checks whether the class is concrete
	 * (i.e. not abstract and not an interface). Can be overridden in subclasses.
	 * @param beanDefinition the bean definition to check
	 * @return whether the bean definition qualifies as a candidate component
	 */
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isConcrete();
	}

}
