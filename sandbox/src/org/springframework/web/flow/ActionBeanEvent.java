/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.EventObject;

import org.springframework.util.ToStringCreator;

/**
 * @author Keith Donald
 */
public class ActionBeanEvent extends EventObject {
    private String id;
    
    public ActionBeanEvent(ActionBean source, String id) {
        super(source);
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof ActionBeanEvent)) {
            return false;
        }
        ActionBeanEvent event = (ActionBeanEvent)o;
        return id.equals(event.id);
    }
    
    public int hashCode() {
        return id.hashCode();
    }
    
    public String toString() {
        return new ToStringCreator(this).append("id", id).toString();
    }
}