/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
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