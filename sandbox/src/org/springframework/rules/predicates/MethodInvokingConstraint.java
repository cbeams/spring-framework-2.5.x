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
package org.springframework.rules.predicates;

import java.lang.reflect.Method;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.reporting.TypeResolvable;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class MethodInvokingConstraint implements UnaryPredicate, TypeResolvable {
    private Object targetObject;
    private Method testMethod;
    private String type;

    public MethodInvokingConstraint(Object targetObject, String methodName,
            Class parameterType) {
        Assert.notNull(targetObject);
        this.targetObject = targetObject;
        try {
            this.testMethod = targetObject.getClass().getMethod(methodName,
                    new Class[] { parameterType });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Class returnType = testMethod.getReturnType();
        Assert.isTrue(returnType == Boolean.class
                || returnType == boolean.class);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean test(Object argument) {
        try {
            return ((Boolean)testMethod.invoke(targetObject,
                    new Object[] { argument })).booleanValue();
        } catch (Exception e) {
            System.out.println("argh");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}