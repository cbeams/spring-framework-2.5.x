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
package org.springframework.jmx.invokers;

import java.util.Arrays;

/**
 * @author Rob Harrop
 *
 */
/**
 * Used to Key method names and signatures
 * for caching method for quick retreival.
 * @author Rob Harrop
 */
public class MethodKey {
	private String name;
	private String[] signature;

	public MethodKey(String name, String[] signature) {
		this.name = name;
		this.signature = signature;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;

		MethodKey otherKey = null;

		if (other instanceof MethodKey) {
			otherKey = (MethodKey) other;

			return name.equals(otherKey.name)
					&& Arrays.equals(signature, otherKey.signature);
		} else {
			return false;
		}
	}
}
