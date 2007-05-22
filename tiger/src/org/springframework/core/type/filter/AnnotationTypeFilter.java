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

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

import org.objectweb.asm.ClassReader;

import org.springframework.core.type.asm.AnnotationMetadataReadingVisitor;

/**
 * Matches classes with a given annotation optionally including inherited annotations.
 *
 * The matching logic mirrors that of <code>Class.isAnnotationPresent()</code>
 * with an additional option to explicitely control if any inherited annotations
 * should be considered.
 *
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.1
 */
public class AnnotationTypeFilter extends AbstractTypeHierarchyTraversingFilter {
	
	private final Class<? extends Annotation> annotationClass;


	/**
	 * @param annotationClass the annotation to match 
	 */
	public AnnotationTypeFilter(Class<? extends Annotation> annotationClass) {
		super(annotationClass.isAnnotationPresent(Inherited.class), false);
		this.annotationClass = annotationClass;
	}


	@Override
	protected boolean matchSelf(ClassReader classReader) {
		AnnotationMetadataReadingVisitor annotationReader = new AnnotationMetadataReadingVisitor();
		classReader.accept(annotationReader, true);
		return annotationReader.hasAnnotation(this.annotationClass.getName());
	}

	@Override
	protected Boolean matchSuperClass(String superClassName) {
		if (Object.class.getName().equals(superClassName)) {
			return Boolean.FALSE;
		}
		else if (superClassName.startsWith("java.")) {
			try {
				Class clazz = getClass().getClassLoader().loadClass(superClassName);
				return Boolean.valueOf(clazz.getAnnotation(this.annotationClass) != null);
			}
			catch (ClassNotFoundException ex) {
				// Class not found - can't determine a match that way.
			}
		}
		return null;
	}

}
