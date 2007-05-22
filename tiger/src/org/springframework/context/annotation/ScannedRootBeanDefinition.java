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

import org.objectweb.asm.ClassReader;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.util.Assert;

/**
 * Extension of the {@link org.springframework.beans.factory.support.RootBeanDefinition}
 * class, based on an ASM ClassReader, with support for annotation metadata exposed
 * through the {@link AnnotatedBeanDefinition} interface.
 *
 * <p>This class does <i>not</i> load the bean <code>Class</code> early.
 * It rather retrieves all relevant metadata from the ".class" file itself,
 * parsed with the ASM ClassReader.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see #getMetadata()
 * @see #getBeanClassName()
 * @see org.objectweb.asm.ClassReader
 */
public class ScannedRootBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {

	private final AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();


	/**
	 * Create a new ScannedRootBeanDefinition for the given ASM ClassReader.
	 * @param classReader the ASM ClassReader for the scanned target class
	 */
	public ScannedRootBeanDefinition(ClassReader classReader) {
		Assert.notNull(classReader, "ClassReader must not be null");
		classReader.accept(this.visitor, true);
	}


	public final AnnotationMetadata getMetadata() {
		return this.visitor;
	}

	public String getBeanClassName() {
		return this.visitor.getClassName();
	}

}
