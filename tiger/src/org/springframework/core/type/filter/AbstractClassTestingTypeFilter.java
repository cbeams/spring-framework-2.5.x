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

import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.asm.ClassMetadataReadingVisitor;
import org.springframework.core.type.asm.ClassReaderFactory;

/**
 * Type filter that exposes a
 * {@link org.springframework.core.type.ClassMetadata} object
 * to subclasses, for class testing purposes.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.5
 * @see #match(org.springframework.core.type.ClassMetadata)
 */
public abstract class AbstractClassTestingTypeFilter implements TypeFilter {

	public final boolean match(ClassReader classReader, ClassReaderFactory classReaderFactory) throws IOException {
		ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
		classReader.accept(visitor, true);
		return match(visitor);
	}

	/**
	 * Determine a match based on the given ClassMetadata object.
	 * @param metadata the ClassMetadata object
	 * @return whether this filter matches on the specified type
	 */
	protected abstract boolean match(ClassMetadata metadata);

}
