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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.asm.CachingClassReaderFactory;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.ClassUtils;

/**
 * A component provider that scans the classpath from a base package (default is empty).
 * It then applies exclude and include filters to the resulting classes to find candidates.
 *
 * <p>This implementation is based on the ASM {@link org.objectweb.asm.ClassReader}.
 *
 * @author Costin Leau
 * @author Rod Johnson
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.1
 * @see org.objectweb.asm.ClassReader
 * @see org.springframework.core.type.asm.AnnotationMetadataReadingVisitor
 */
public class ClassPathScanningCandidateComponentProvider implements CandidateComponentProvider, ResourceLoaderAware {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String[] packageSearchPaths;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private ClassReaderFactory classReaderFactory = new CachingClassReaderFactory(this.resourcePatternResolver);

	private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();

	private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();


	public ClassPathScanningCandidateComponentProvider(String basePackage) {
		this(new String[] {basePackage}, true);
	}

	public ClassPathScanningCandidateComponentProvider(String basePackage, boolean useDefaultFilters) {
		this(new String[] {basePackage}, useDefaultFilters);
	}

	public ClassPathScanningCandidateComponentProvider(String[] basePackages, boolean useDefaultFilters) {
		this.packageSearchPaths = new String[basePackages.length];
		for (int i = 0; i < basePackages.length; i++) {
			this.packageSearchPaths[i] =
					"classpath*:" + ClassUtils.convertClassNameToResourcePath(basePackages[i]) + "/**/*.class";
		}
		if (useDefaultFilters) {
			initDefaultFilters();
		}
	}


	@SuppressWarnings("unchecked")
	private void initDefaultFilters() {
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		this.includeFilters.add(new AnnotationTypeFilter(Repository.class));
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.classReaderFactory = new CachingClassReaderFactory(resourceLoader);
	}

	public void addExcludeFilter(TypeFilter excludeFilter) {
		// add exclude filters to the front of the list
		this.excludeFilters.add(0, excludeFilter);
	}

	public void addIncludeFilter(TypeFilter includeFilter) {
		// add include filters to the end of the list
		this.includeFilters.add(includeFilter);
	}


	public Set<BeanDefinition> findCandidateComponents() {
		Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
		for (int i = 0; i < this.packageSearchPaths.length; i++) {
			try {
				Resource[] resources = this.resourcePatternResolver.getResources(this.packageSearchPaths[i]);
				for (int j = 0; j < resources.length; j++) {
					Resource resource = resources[j];
					ClassReader classReader = getClassReaderIfCandidate(resource);
					if (classReader != null) {
						ScannedRootBeanDefinition sbd = new ScannedRootBeanDefinition(classReader);
						sbd.setSource(resource);
						if (sbd.getMetadata().isConcrete()) {
							candidates.add(sbd);
						}
					}
				}
			}
			catch (IOException ex) {
				throw new FatalBeanException("I/O failure during classpath scanning", ex);
			}
		}
		return candidates;
	}

	private ClassReader getClassReaderIfCandidate(Resource resource) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Checking for candidate: " + resource);
		}
		ClassReader classReader = this.classReaderFactory.getClassReader(resource);
		if (isCandidateComponent(classReader)) {
			return classReader;
		}
		return null;
	}

	/**
	 * @return true if this class does not match any exclude filter
	 * and does match at least one include filter
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

}
