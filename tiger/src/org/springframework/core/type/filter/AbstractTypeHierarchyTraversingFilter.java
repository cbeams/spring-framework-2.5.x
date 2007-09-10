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

package org.springframework.core.type.filter;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.core.type.asm.ClassMetadataReadingVisitor;

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
 * @since 2.5
 */
public abstract class AbstractTypeHierarchyTraversingFilter implements TypeFilter {

	private final boolean considerInherited;

	private final boolean considerInterfaces;


	protected AbstractTypeHierarchyTraversingFilter(boolean considerInherited, boolean considerInterfaces) {
		this.considerInherited = considerInherited;
		this.considerInterfaces = considerInterfaces;
	}


	public boolean match(ClassReader classReader, ClassReaderFactory classReaderFactory) throws IOException {
		// This method optimizes avoiding unnecessary creation of ClassReaders
		// as well as visiting over those readers.
		if (matchSelf(classReader)) {
			return true;
		}
		ClassMetadataReadingVisitor typesReadingVisitor = new ClassMetadataReadingVisitor();
		classReader.accept(typesReadingVisitor, true);
		if (matchClassName(typesReadingVisitor.getClassName())) {
			return true;
		}

		if (!this.considerInherited) {
			return false;
		}
		if (typesReadingVisitor.hasSuperClass()) {
			// Optimization to avoid creating ClassReader for super class.
			Boolean superClassMatch = matchSuperClass(typesReadingVisitor.getSuperClassName());
			if (superClassMatch != null) {
				if (superClassMatch.booleanValue()) {
					return true;
				}
			}
			else {
				// Need to read super class to determine a match...
				if (match(typesReadingVisitor.getSuperClassName(), classReaderFactory)) {
					return true;
				}
			}
		}

		if (!this.considerInterfaces) {
			return false;
		}
		for (String ifc : typesReadingVisitor.getInterfaceNames()) {
			// Optimization to avoid creating ClassReader for super class
			Boolean interfaceMatch = matchInterface(ifc);
			if (interfaceMatch != null) {
				if (interfaceMatch.booleanValue()) {
					return true;
				}
			}
			else {
				// Need to read interface to determine a match...
				if (match(ifc, classReaderFactory)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean match(String className, ClassReaderFactory classReaderFactory) throws IOException {
		return match(classReaderFactory.getClassReader(className), classReaderFactory);
	}

	/**
	 * Override this to match self characteristics alone. Typically,
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

	/**
	 * Override this to match on super type name.
	 */
	protected Boolean matchSuperClass(String superClassName) {
		return null;
	}

	/**
	 * Override this to match on interface type name.
	 */
	protected Boolean matchInterface(String interfaceNames) {
		return null;
	}

}
