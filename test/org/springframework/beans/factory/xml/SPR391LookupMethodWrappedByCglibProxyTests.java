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

package org.springframework.beans.factory.xml;

import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.test.AbstractSpringContextTests;

/**
 * 
 * @author Rod Johnson
 */
public class SPR391LookupMethodWrappedByCglibProxyTests extends AbstractDependencyInjectionSpringContextTests {

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { "/org/springframework/beans/factory/xml/overloadOverrides.xml" };
    }
    
    // Confirm presence of bug. 
    // TODO fix this
    public void testSPR391() {
        try {
            BUGtestAutoProxiedLookup();          
            fail();
        }
        catch (BeanCreationException ex) {
            System.err.println("****** SPR-391:  Cannot use CGLIB proxying around beans with method lookup");
        }
    }
    
    public void BUGtestAutoProxiedLookup() {
        OverloadLookup olup = (OverloadLookup) applicationContext.getBean("autoProxiedOverload");
        ITestBean jenny = olup.newTestBean();
        assertEquals("Jenny", jenny.getName());
    }
    
    public void BUGtestRegularlyProxiedLookup() {
        OverloadLookup olup = (OverloadLookup) applicationContext.getBean("regularlyProxiedOverload");
        ITestBean jenny = olup.newTestBean();
        assertEquals("Jenny", jenny.getName());
    }
    
    public static abstract class OverloadLookup {
        
        public abstract ITestBean newTestBean();
        
//        public void overloaded() {            
//        }
       
//        public void overloaded(String s) {   
//        }
//        
//        public String overloaded(float f) {
//            return "";
//        }
    }

}
