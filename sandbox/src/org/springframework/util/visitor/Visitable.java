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
package org.springframework.util.visitor;

/**
 * The super vistable interface of the visitor design pattern.
 * 
 * @author Keith Donald
 */
public interface Visitable {

    /**
     * Accept a visitor and perform a dispatch. Within accept(), Vistables pass
     * themselves back to the visitor. The visitor then executes an encapsulated
     * algorithm unique to that type of Visitable object. As an alternative to
     * defining specific Vistable implementations, consider using the generic
     * <code>ReflectiveVisitorSupport</code>.
     * 
     * @param visitor
     *            The visitor to accept.
     */
    public void accept(Visitor visitor);
}