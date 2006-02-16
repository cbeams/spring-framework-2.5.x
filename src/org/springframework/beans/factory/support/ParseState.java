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

package org.springframework.beans.factory.support;

import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Stack;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ParseState {

	private Stack state = new Stack();

	public void bean(String beanName) {
		if (!empty() && current().getType() == Entry.TYPE_BEAN) {
			throw new IllegalStateException("Cannot parse 'bean' directly after another 'bean'");
		}
		this.state.push(new Entry(StringUtils.hasText(beanName) ? beanName : "<anonymous>", Entry.TYPE_BEAN));
	}

	public void property(String propertyName) {
		if (empty()) {
			throw new IllegalStateException("'property' cannot be root of parse state");
		}
		if (current().getType() == Entry.TYPE_PROPERTY) {
			throw new IllegalStateException("Cannot parse 'property' directly after another 'property'");
		}

		this.state.push(new Entry(propertyName, Entry.TYPE_PROPERTY));
	}

	public boolean empty() {
		return this.state.empty();
	}

	public Entry current() {
		return (Entry) this.state.peek();
	}

	public void pop() {
		this.state.pop();
	}

	public ParseState snapshot() {
		ParseState other = new ParseState();
		other.state = (Stack) this.state.clone();
		return other;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		Iterator itr = this.state.listIterator();
		while (itr.hasNext()) {
			Entry entry = (Entry) itr.next();
			if (entry.getType() == Entry.TYPE_BEAN) {
				sb.append(entry.getName());
			}
			else if (entry.getType() == Entry.TYPE_PROPERTY) {
				sb.append('(').append(entry.getName()).append(')');

				if (itr.hasNext()) {
					sb.append(" -> ");
				}
			}
		}
		return sb.toString();
	}

	public void constructor() {
		constructor(-1);
	}

	public void constructor(int index) {
		if (empty()) {
			throw new IllegalStateException("'constructor' cannot be root of parse state");
		}
		if (current().getType() == Entry.TYPE_CONSTRUCTOR) {
			throw new IllegalStateException("Cannot parse 'constructor' directly after another 'constructor'");
		}

		String name = (index >= 0 ? ".ctor#" + index : ".ctor");
		this.state.push(new Entry(name, Entry.TYPE_CONSTRUCTOR));
	}

	public static class Entry {

		public static final int TYPE_BEAN = 0;

		public static final int TYPE_PROPERTY = 1;

		public static final int TYPE_CONSTRUCTOR = 2;

		private final String name;

		private final int type;

		public Entry(String name, int type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public int getType() {
			return type;
		}
	}

}
