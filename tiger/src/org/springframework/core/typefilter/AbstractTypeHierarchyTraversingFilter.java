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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;

/**
 * Type filter that is aware of traversing over hierarchy.
 * 
 * <p>This filter is useful when matching needs to be made based on potentially the
 * whole class/interface hierarchy. The algorithm employed uses succeed-fast
 * strategy i.e. if at anytime a match is declared, no further processing is
 * carried out.
 * 
 * @author Ramnivas Laddad
 * @since 2.1
 */
public abstract class AbstractTypeHierarchyTraversingFilter implements TypeFilter {
	protected final Log log = LogFactory.getLog(getClass());

	private boolean considerInherited;

	public AbstractTypeHierarchyTraversingFilter(boolean considerInherited) {
		this.considerInherited = considerInherited;
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

		if (!considerInherited) {
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
		try {
			return match(new ClassReader(className));
		}
		catch (IOException ex) {
			log.error("Failed loading: " + className);
			throw new IllegalArgumentException("Cannot load class with name '" + className + "'");
		}
	}

	/**
	 * Override this to match self characterisitcs alone. Typically,
	 * implementation will use a visitor to extract information to peform
	 * matching.
	 * 
	 * @param classReader
	 * @return
	 * 
	 */
	protected boolean matchSelf(ClassReader classReader) {
		return false;
	}

	/**
	 * Override this to match on type name
	 * @param className
	 * @return
	 */
	protected boolean matchClassName(String className) {
		return false;
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	protected boolean matchSuperClassName(String superClassName) {
		return false;
	}

	protected boolean matchInterfaceName(String interfaceNames) {
		return false;
	}
}
