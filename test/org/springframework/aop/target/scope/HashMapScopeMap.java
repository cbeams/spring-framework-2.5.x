/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.aop.target.scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Trivial implementation of ScopeMap interface, using a simple Java HashMap.
 *
 * @author Rod Johnson
 */
public class HashMapScopeMap implements ScopeMap {

	private Map map;

	private final boolean persistent;


	public HashMapScopeMap() {
		this.persistent = false;
	}

	public HashMapScopeMap(boolean persistent) {
		this.persistent = persistent;
	}

	public void initScope() {
		this.map = new HashMap();
	}


	public int getSize() {
		return this.map.size();
	}

	public boolean isPersistent() {
		return persistent;
	}

	public Object get(String name) {
		return this.map.get(name);
	}

	public void put(String name, Object value) {
		this.map.put(name, value);
	}

	public void remove(String name) {
		this.map.remove(name);
	}

}
