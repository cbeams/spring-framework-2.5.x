/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.value;


/**
 * A container for a value that can change. This container encapsulates /
 * abstracts away the logic necessary to access, set, and be notified of changes
 * in the contained value.
 * 
 * The value model pattern greatly simplifies client(s) access to a mutable
 * value, as clients access the value in a consistent manner (through this
 * common interface). Clients may also use this interface to subscribe for value
 * change events.
 * 
 * A good example of the ValueModel pattern is the ThreadLocal object. Another
 * example is a mediator between GUI controls and the domain model.
 * 
 * @author Keith Donald
 */
public interface ValueModel extends ValueChangePublisher {

    /**
     * Get the value contained by this model.
     * 
     * @return the current value
     */
    public Object getValue();

    /**
     * Set (or replace if the value is already set) the value contained by this
     * model.
     * 
     * @param value
     *            the new value
     */
    public void setValue(Object value);
}