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

package org.springframework.beans.factory.support;

import java.util.LinkedHashMap;

/**
 * Tag subclass used to hold managed Map values, which may
 * include runtime bean references.
 *
 * <p>Just used on JDK >= 1.4, as java.util.LinkedHashMap -
 * which preserves key order - is not available for earlier JDKs.
 *
 * @author Juergen Hoeller
 * @since 02.06.2004
 * @see ManagedMap
 * @see java.util.LinkedHashMap
 */
public class ManagedLinkedMap extends LinkedHashMap {

	public ManagedLinkedMap() {
	}

	public ManagedLinkedMap(int initialCapacity) {
		super(initialCapacity);
	}

}
