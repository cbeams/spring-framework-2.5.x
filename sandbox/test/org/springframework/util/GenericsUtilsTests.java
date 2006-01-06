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

package org.springframework.util;

import junit.framework.TestCase;

/**
 * @author Juergen Hoeller
 * @since 15.12.2005
 */
public class GenericsUtilsTests extends TestCase {

	/*
	public void testDetermineCollectionType() throws Exception {
		Method method = getClass().getMethod("testMethod1", new Class[] {Collection.class});
		assertEquals(String.class, GenericsUtils.determineCollectionType(method, 0));
	}

	public void testCannotDetermineCollectionType() throws Exception {
		Method method = getClass().getMethod("testMethod2", new Class[] {Collection.class});
		assertEquals(null, GenericsUtils.determineCollectionType(method, 0));
	}

	public void testDetermineMapTypes() throws Exception {
		Method method = getClass().getMethod("testMethod3", new Class[] {Map.class});
		assertEquals(String.class, GenericsUtils.determineMapKeyType(method, 0));
		assertEquals(Integer.class, GenericsUtils.determineMapValueType(method, 0));
	}

	public void testCannotDetermineMapTypes() throws Exception {
		Method method = getClass().getMethod("testMethod4", new Class[] {Map.class});
		assertEquals(null, GenericsUtils.determineMapKeyType(method, 0));
		assertEquals(null, GenericsUtils.determineMapValueType(method, 0));
	}

	public void testMethod1(Collection<String> arg) {
	}

	public void testMethod2(Collection arg) {
	}

	public void testMethod3(Map<String,Integer> arg) {
	}

	public void testMethod4(Map arg) {
	}
	*/

}
