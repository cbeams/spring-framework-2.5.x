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