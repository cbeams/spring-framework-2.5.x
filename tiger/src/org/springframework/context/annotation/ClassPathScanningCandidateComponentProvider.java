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
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.typefilter.AnnotationTypeFilter;
import org.springframework.core.typefilter.ClassNameAndTypesReadingVisitor;
import org.springframework.core.typefilter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.ClassUtils;

/**
 * A component provider that scans the classpath from a base package (default is empty).
 * It then applies exclude and include filters to the resulting classes to find candidates.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.1
 */
public class ClassPathScanningCandidateComponentProvider
		implements CandidateComponentProvider, ResourceLoaderAware {

	private static final String CLASS_FILE_EXTENSION = ".class";


	protected final Log logger = LogFactory.getLog(getClass());

	private final String basePackage;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();

	private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

	
	public ClassPathScanningCandidateComponentProvider(String basePackage) {
		this.basePackage = ClassUtils.convertClassNameToResourcePath(basePackage);
	}

	public ClassPathScanningCandidateComponentProvider(String basePackage, boolean useDefaultFilters) {
		this(basePackage);
		if (useDefaultFilters) {
			initDefaultFilters();
		}
	}


	@SuppressWarnings("unchecked")
	private void initDefaultFilters() {
		this.includeFilters.add(new AnnotationTypeFilter(Component.class, true));
		this.includeFilters.add(new AnnotationTypeFilter(Repository.class, true));
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					ClassUtils.forName("org.aspectj.lang.annotation.Aspect", getClass().getClassLoader()), false));
		}
		catch (ClassNotFoundException ex) {
			// will not scan for @Aspect annotations if not present
		}
	}
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}

	public void addExcludeFilter(TypeFilter excludeFilter) {
		// add exclude filters to the front of the list
		this.excludeFilters.add(0, excludeFilter);
	}

	public void addIncludeFilter(TypeFilter includeFilter) {
		// add include filters to the end of the list
		this.includeFilters.add(includeFilter);
	}

	public Set<Class> findCandidateComponents() {
		Set<Class> candidates = new HashSet<Class>();
		String scanPath = "classpath*:" + this.basePackage + "/**/*.class";
		
		try {
			Resource[] resources = this.resourcePatternResolver.getResources(scanPath);
			for (int i = 0; i < resources.length; i++) {
				Class clazz = loadClassIfCandidate(resources[i]);
				if (clazz != null) {
					candidates.add(clazz);
				}
			}
		}
		catch (IOException e) {
			throw new FatalBeanException("failed in classpath scan", e);
		}
		return candidates;
	}

	private Class loadClassIfCandidate(Resource resource) throws IOException {
		String name = resource.getFilename();
		if (logger.isDebugEnabled()) {
			logger.debug("Checking for candidate: " + resource);
		}

		if (!name.endsWith(CLASS_FILE_EXTENSION)) {
			return null;
		}

		InputStream stream = resource.getInputStream();
		try {
			ClassReader classReader = new ClassReader(stream);
			ClassNameAndTypesReadingVisitor nameReader = new ClassNameAndTypesReadingVisitor();
			classReader.accept(nameReader, true);

			if (isCandidateComponent(classReader)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found candidate - loading class: " + nameReader.getClassName());
				}
				Class clazz = loadClass(nameReader.getClassName());
				if (!clazz.isInterface()) {
					return clazz;
				}
			}
		}
		finally {
			try {
				if (stream != null) {
					stream.close();
				}
			}
			catch (IOException e) {
				// ignore, cleanup
			}
		}
		return null;
	}

	/**
	 * 
	 * @param classReader ASM ClassReader for the class
	 * @return true if this class does not match any exclude filter 
	 *              and does match at least one include filter
	 */
	protected boolean isCandidateComponent(ClassReader classReader) {
		for (TypeFilter tf : this.excludeFilters) {
			if (tf.match(classReader)) {
				return false;
			}
		}
		for (TypeFilter tf : this.includeFilters) {
			if (tf.match(classReader)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Utility method which loads a class without initializing it.
	 * Translates any ClassNotFoundException into BeanDefinitionStoreException.
	 * @param className the name of the class
	 * @return the loaded class
	 */
	protected Class loadClass(String className) {
		try {
			return Class.forName(ClassUtils.convertResourcePathToClassName(className),
					false, this.resourcePatternResolver.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException("Unable to load class: " + className, ex);
		}
	}

}
