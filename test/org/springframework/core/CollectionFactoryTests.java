/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.core;

import java.util.Map;

import junit.framework.TestCase;



/**
 * CollectionFactoryTests
 * 
 * @author Darren Davison
 * @since 1.1.3
 */
public class CollectionFactoryTests extends TestCase {
    
    /**
     * initial capacity of 0 works with JDK1.4 classes, but not the commons
     * collections (which will be used under the hood in a JDK1.3 platform)
     */
    public void testLinkedMapWithZeroCapacity() {
        try {
            Map m = CollectionFactory.createLinkedMapIfPossible(0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testIdentityMapWithZeroCapacity() {
        try {
            Map m = CollectionFactory.createIdentityMapIfPossible(0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
