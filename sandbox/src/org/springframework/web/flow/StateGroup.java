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
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.Algorithms;

/**
 * @author Keith Donald
 */
public class StateGroup implements Serializable {
	private String id;

	private Set states = new LinkedHashSet(6);

	private Flow flow;

	public StateGroup(Flow flow, String id) {
		Assert.hasText("The state group is required");
		this.flow = flow;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean add(AbstractState state) {
		boolean added = states.add(states);
		if (added) {
			state.setFlow(flow);
		}
		return added;
	}

	public boolean addAll(AbstractState[] states) {
		boolean anyAdded = false;
		for (int i = 0; i < states.length; i++) {
			boolean added = add(states[i]);
			if (!anyAdded && added) {
				anyAdded = true;
			}
		}
		return anyAdded;
	}

	public boolean equals(Object o) {
		if (!(o instanceof StateGroup)) {
			return false;
		}
		StateGroup g = (StateGroup)o;
		return id.equals(g.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public Iterator iterator() {
		return states.iterator();
	}

	public AbstractState findFirst(Constraint constraint) {
		return (AbstractState)Algorithms.instance().findFirst(states, constraint);
	}

	public String toString() {
		return "group:" + getId() + "<" + DefaultObjectStyler.call(states) + ">";
	}
}