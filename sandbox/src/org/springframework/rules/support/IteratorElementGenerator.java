/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.rules.support;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.Closure;
import org.springframework.rules.Generator;

public class IteratorElementGenerator implements Generator {
    private Iterator it;

    public IteratorElementGenerator(Collection collection) {
        this(collection.iterator());
    }

    public IteratorElementGenerator(Iterator it) {
        this.it = it;
    }

    public void run(Closure elementCallback) {
        while (it.hasNext()) {
            elementCallback.call(it.next());
        }
    }
}