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
import org.springframework.util.ToStringCreator;

/**
 * @author Keith Donald
 */
public class StateGroups implements Serializable {
	
    public static final String DEFAULT_GROUP_ID = "main";

    private Set stateGroups = new LinkedHashSet(6);

    public void add(AbstractState state) {
        add(DEFAULT_GROUP_ID, state);
    }

    public boolean add(String groupId, AbstractState state) {
        return getOrCreateGroup(groupId).add(state);
    }

    public boolean addAll(AbstractState[] states) {
        return addAll(DEFAULT_GROUP_ID, states);
    }

    public boolean addAll(String groupId, AbstractState[] states) {
        return getOrCreateGroup(groupId).addAll(states);
    }

    private StateGroup getOrCreateGroup(String groupId) {
        StateGroup group = getGroup(groupId);
        if (group == null) {
            group = new StateGroup(groupId);
            add(group);
        }
        return group;
    }

    public StateGroup getGroup(String groupId) {
        Iterator it = stateGroups.iterator();
        while (it.hasNext()) {
            StateGroup group = (StateGroup)it.next();
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    public boolean add(StateGroup stateGroup) {
        Assert.notNull(stateGroup, "The state group to add is required");
        return stateGroups.add(stateGroup);
    }

    public Iterator statesIterator() {
        return new StatesIterator();
    }

    public boolean equals(Object o) {
        if (!(o instanceof StateGroups)) {
            return false;
        }
        StateGroups g = (StateGroups)o;
        return stateGroups.equals(g.stateGroups);
    }

    public int hashCode() {
        return stateGroups.hashCode();
    }

    public class StatesIterator implements Iterator {
        private Iterator groupIterator = stateGroups.iterator();

        private Iterator statesIterator;

        public boolean hasNext() {
            return groupIterator.hasNext() || statesIterator.hasNext();
        }

        public Object next() {
            if (statesIterator == null) {
                statesIterator = ((StateGroup)groupIterator.next()).iterator();
            }
            if (statesIterator.hasNext()) {
                return statesIterator.next();
            }
            else {
                statesIterator = ((StateGroup)groupIterator.next()).iterator();
                return next();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isEmpty() {
        return stateGroups.isEmpty();
    }

    public StateGroup getLastGroup() {
        Iterator it = stateGroups.iterator();
        StateGroup lastGroup = null;
        while (it.hasNext()) {
            lastGroup = (StateGroup)it.next();
        }
        return lastGroup;
    }

    public String toString() {
        return new ToStringCreator(this).append("stateGroups", stateGroups).toString();
    }
}