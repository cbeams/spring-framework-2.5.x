/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.thread;

public interface ThreadLocalStorage {

	/**
	 * Returns the thread-local object for the given key, or null if no such
	 * object exists.
	 */
	public Object get(Object key);

	/**
	 * Stores the value object at the given key, overwriting any prior value
	 * that may have been stored at that key. Care should be taken in selecting
	 * keys to avoid naming conflicts; in general, prefixing a key with a module
	 * id is a good idea.
	 */
	public void put(Object key, Object value);

	/**
	 * Clears all keys.
	 */
	public void clear();
}
