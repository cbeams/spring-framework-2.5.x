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

import java.util.HashMap;

/**
 * Tag subclass used to hold managed Map values, which may
 * include runtime bean references.
 *
 * <p>Just used on JDK < 1.4, as java.util.LinkedHashMap -
 * which preserves key order - is preferred when it is available. 
 *
 * @author Rod Johnson
 * @since 27-May-2003
 * @version $Id: ManagedMap.java,v 1.3 2004-06-02 17:10:48 jhoeller Exp $
 * @see ManagedLinkedMap
 * @see java.util.LinkedHashMap
 */
public class ManagedMap extends HashMap {

	public ManagedMap() {
	}

	public ManagedMap(int initialCapacity) {
		super(initialCapacity);
	}

}
