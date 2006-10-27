/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.mock.web;

import org.springframework.util.Assert;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;

/**
 * Enumerates {@link HeaderValueHolder} names.
 * 
 * @author Rick Evans
 */
final class HttpHeaderNamesEnumerator implements Enumeration {

	private Iterator httpHeaderIterator;


	/**
	 * Crate a new instance of the {@link HttpHeaderNamesEnumerator} class.
	 * @param headers the collection of {@link HeaderValueHolder headers} to be enumerated
	 */
	HttpHeaderNamesEnumerator(Collection headers) {
		Assert.notNull("Headers must not be null.");
		this.httpHeaderIterator = headers.iterator();
	}


	public boolean hasMoreElements() {
		return this.httpHeaderIterator.hasNext();
	}

	public Object nextElement() {
		return ((HeaderValueHolder) this.httpHeaderIterator.next()).getName();
	}

}
