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

import java.util.HashSet;
import java.util.Set;


/**
 * @author Rod Johnson
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @since 2.1
 */
public class AssignableTypeFilter extends AbstractTypeHierarchyTraversingFilter {
	public final String typeName;
	
	/**
	 * @param type type to match
	 */
	public AssignableTypeFilter(Class type) {
		super(true, true);
		this.typeName = type.getName();
	}
	
	@Override
	protected boolean matchClassName(String className) {
		return matchType(className);
	}

	@Override
	protected boolean matchSuperClassName(String superClassName) {
		return matchType(superClassName);
	}
	
	@Override
	protected boolean matchInterfaceName(String interfaceName) {
		return matchType(interfaceName);
	}
	
	private boolean matchType(String typeName) {
		return this.typeName.equals(typeName);
	}
}
