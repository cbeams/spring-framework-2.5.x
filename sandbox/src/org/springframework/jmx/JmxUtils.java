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
package org.springframework.jmx;

import java.lang.reflect.Method;

import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 * @author Rob Harrop
 */
public class JmxUtils {

    private static final String GET = "get";

    private static final String SET = "set";

    /**
     * Determines whether the supplied method is actually the getter or setter
     * for a JavaBean style property.
     * 
     * @param method
     * @return
     */
    public static boolean isProperty(Method method) {

        String name = method.getName();

        // name should be at least four chars to be a property
        if(name.length() < 4) {
            return false;
        }
        
        // the fourth character should be uppercase
        if (!Character.isUpperCase(name.charAt(3))) {
            return false;
        }

        // check that method name starts
        // with either get or set
        if (name.startsWith(GET)) {
            return ((method.getReturnType() != void.class) && (method.getParameterTypes().length == 0));
        } else if (name.startsWith(SET)) {
            return ((method.getReturnType() == void.class) && (method.getParameterTypes().length == 1));
        } else {
            return false;
        }
    }

    public static ModelMBeanOperationInfo[] shrink(ModelMBeanOperationInfo[] source, int count) {
        ModelMBeanOperationInfo[] dest = new ModelMBeanOperationInfo[count];
        for (int x = 0; x < count; x++) {
            dest[x] = source[x];
        }
        return dest;
    }

    public static ModelMBeanAttributeInfo[] shrink(ModelMBeanAttributeInfo[] source, int count) {
        ModelMBeanAttributeInfo[] dest = new ModelMBeanAttributeInfo[count];
        for (int x = 0; x < count; x++) {
            dest[x] = source[x];
        }
        return dest;
    }
}