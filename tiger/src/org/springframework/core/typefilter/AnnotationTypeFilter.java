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

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Matches classes with a given annotation optionally including inherited annotations.
 * 
 * The matching logic mirrors that of Class.isAnnotationPresent() with an additional option
 * to explicitely control if any inherited annotatins should be considered.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @since 2.1
 */
public class AnnotationTypeFilter extends AbstractTypeHierarchyTraversingFilter {
	
	private final Class<? extends Annotation> annotationClass;


	/**
	 * @param annotationClass The annotation to match 
	 * @param considerInherited search inheritance hierarchy for the annotation
	 */
	public AnnotationTypeFilter(Class<? extends Annotation> annotationClass, boolean considerInherited) {
		super(considerInherited && annotationClass.isAnnotationPresent(Inherited.class), false);
		this.annotationClass = annotationClass;
	}
	
	/**
	 * @param annotationClass The annotation to match
	 */
	public AnnotationTypeFilter(final Class<? extends Annotation> annotationClass) {
		this(annotationClass, true);
	}


	@Override
	protected boolean matchSelf(ClassReader classReader) {
		AnnotationReadingVisitor annotationReader = new AnnotationReadingVisitor();
		classReader.accept(annotationReader, true);
		for (String annotationType : annotationReader.getAnnotationNames()) {
			if (annotationClass.getName().equals(annotationType)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * ASM annotation visitor used for reading innerClasses without loading the
	 * class. The reader is simplified at considers only the innerClasses
	 * description and not their value.
	 */
	private static class AnnotationReadingVisitor extends EmptyVisitor {

		private final List<String> annotations = createList();

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			this.annotations.add(Type.getType(desc).getClassName());
			return super.visitAnnotation(desc, visible);
		}

		public AnnotationVisitor visitAnnotation(String name, String desc) {
			annotations.add(Type.getType(desc).getClassName());
			return super.visitAnnotation(name, desc);
		}

		protected List<String> createList() {
			return new ArrayList<String>();
		}
		
		public List<String> getAnnotationNames() {
			return this.annotations;
		}
	}

}
