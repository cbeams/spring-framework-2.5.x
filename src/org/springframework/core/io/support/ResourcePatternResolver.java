/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.core.io.support;

import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Strategy interface for resolving a location pattern into Resource objects.
 *
 * <p>Can be used with any sort of location pattern: Input patterns have
 * to match the strategy implementation. This interface just specifies
 * the conversion method rather than a specific pattern format.
 *
 * @author Juergen Hoeller
 * @since 01.05.2004
 * @see PathMatchingResourcePatternResolver
 */
public interface ResourcePatternResolver {

	/**
	 * Resolve the given location pattern into Resource objects.
	 * @param locationPattern the location pattern to resolve
	 * @return the corresponding Resource objects
	 * @throws IOException in case of I/O errors
	 */
	Resource[] getResources(String locationPattern) throws IOException;

}
