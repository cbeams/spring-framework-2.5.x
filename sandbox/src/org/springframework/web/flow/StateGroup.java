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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.Algorithms;

/**
 * @author Keith Donald
 */
public class StateGroup implements Serializable {
    private String id;

    private Set states = new LinkedHashSet(6);

    public StateGroup(String id) {
        Assert.hasText("The state group is required");
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean add(AbstractState state) {
        return states.add(states);
    }

    public boolean addAll(AbstractState[] states) {
        return this.states.addAll(Arrays.asList(states));
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
        return new ToStringCreator(this).append("id", id).append("states", states).toString();
    }
}