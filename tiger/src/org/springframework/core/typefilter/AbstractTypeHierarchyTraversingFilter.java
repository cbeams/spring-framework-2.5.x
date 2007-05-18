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

package org.springframework.core.typefilter;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Type filter that is aware of traversing over hierarchy.
 * 
 * <p>This filter is useful when matching needs to be made based on potentially the
 * whole class/interface hierarchy. The algorithm employed uses succeed-fast
 * strategy i.e. if at anytime a match is declared, no further processing is
 * carried out.
 * 
 * @author Ramnivas Laddad
 * @author Mark Fisher
 * @since 2.1
 */
public abstract class AbstractTypeHierarchyTraversingFilter implements TypeFilter {

	private final boolean considerInherited;

	private final boolean considerInterfaces;


	public AbstractTypeHierarchyTraversingFilter(boolean considerInherited, boolean considerInterfaces) {
		this.considerInherited = considerInherited;
		this.considerInterfaces = considerInterfaces;
	}

	public boolean match(ClassReader classReader) {
		// This method optimizes avoiding unnecessary creation of ClassReaders
		// as well as visting over those readers
		if (matchSelf(classReader)) {
			return true;
		}
		ClassNameAndTypesReadingVisitor typesReadingVisitor = new ClassNameAndTypesReadingVisitor();
		classReader.accept(typesReadingVisitor, true);

		if (matchClassName(typesReadingVisitor.getClassName())) {
			return true;
		}

		if (!this.considerInherited) {
			return false;
		}

		if (typesReadingVisitor.hasSuperClass()) {
			// Optimization to avoid creating ClassReader for super class
			if (matchSuperClassName(typesReadingVisitor.getSuperName())) {
				return true;
			}
			if (match(typesReadingVisitor.getSuperName())) {
				return true;
			}
		}

		if (!this.considerInterfaces) {
			return false;
		}

		for (String interfaze : typesReadingVisitor.getInterfaceNames()) {
			// Optimization to avoid creating ClassReader for super class
			if (matchInterfaceName(interfaze)) {
				return true;
			}
			if (match(interfaze)) {
				return true;
			}
		}
		return false;
	}

	private boolean match(String className) {
		String resourcePath = ClassUtils.convertClassNameToResourcePath(className) + ".class";
		Resource resource = new ClassPathResource(resourcePath);
		InputStream stream = null;
		try {
			stream = resource.getInputStream();
			return match(new ClassReader(stream));
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Cannot load class with name '" + className + "'", ex);
		}
		finally {
			try {
				if (stream != null) {
					stream.close();
				}
			}
			catch (IOException ex) {
				// ignore, cleanup
			}
		}
	}

	/**
	 * Override this to match self characterisitcs alone. Typically,
	 * the implementation will use a visitor to extract information
	 * to perform matching.
	 */
	protected boolean matchSelf(ClassReader classReader) {
		return false;
	}

	/**
	 * Override this to match on type name.
	 */
	protected boolean matchClassName(String className) {
		return false;
	}

	protected boolean matchSuperClassName(String superClassName) {
		return false;
	}

	protected boolean matchInterfaceName(String interfaceNames) {
		return false;
	}

}
