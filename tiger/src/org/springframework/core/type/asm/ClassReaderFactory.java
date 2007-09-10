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

package org.springframework.core.type.asm;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.Resource;

/**
 * Factory interface for ASM {@link org.objectweb.asm.ClassReader} instances.
 * Allows for caching a ClassReader per original resource.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.objectweb.asm.ClassReader
 */
public interface ClassReaderFactory {

	/**
	 * Obtain a ClassReader for the given class name.
	 * @param className the class name (to be resolved to a ".class" file)
	 * @return the ClassReader instance (never <code>null</code>)
	 * @throws IOException in case of I/O failure
	 */
	ClassReader getClassReader(String className) throws IOException;

	/**
	 * Obtain a ClassReader for the given resource.
	 * @param resource the resource (pointing to a ".class" file)
	 * @return the ClassReader instance (never <code>null</code>)
	 * @throws IOException in case of I/O failure
	 */
	ClassReader getClassReader(Resource resource) throws IOException;

}
