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
package org.springframework.jmx.adapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

/**
 * @author robh
 */
public abstract class AbstractReflectionBasedAdapterHost extends
        AbstractConfigurableAdapterHost {

    private Object adapter;

    private Class adapterClass;

    private Method startMethod;

    private Method stopMethod;

    protected void initAdapterHost() {

        preInit();
        
        createAdapter();
        locateStartMethod();
        locateStopMethod();
        
        postInit();

    }

    private void createAdapter() {
        try {
            adapterClass = ClassUtils.forName(getClassName());

            Constructor constructor = adapterClass.getConstructor(getConstructorArgumentTypes());

            adapter = BeanUtils.instantiateClass(constructor,
                    getConstructorArguments());

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Adapter Class: "
                    + getClassName() + " could not be found.");
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    "Could not locate constructor with specified signature");
        }
    }

    private void locateStartMethod() {
        try {
            startMethod = adapterClass.getMethod(getStartMethodName(), null);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    "Unable to locate start method for adapter.");
        }
    }

    private void locateStopMethod() {
        try {
            stopMethod = adapterClass.getMethod(getStopMethodName(), null);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    "Unable to locate stop method for adapter.");
        }
    }

    public void start() {
        try {
            startMethod.invoke(adapter, null);
        } catch (IllegalAccessException ex) {
            throw new AdapterStartupException(
                    "IllegalAccessException occured when trying to invoke adapter startup method.",
                    ex);
        } catch (InvocationTargetException ex) {
            throw new AdapterStartupException(
                    "Exception occured when starting adapter.", ex);
        }
    }

    public void stop() {
        try {
            stopMethod.invoke(adapter, null);
        } catch (IllegalAccessException ex) {
            throw new AdapterStartupException(
                    "IllegalAccessException occured when trying to invoke adapter stopping method.",
                    ex);
        } catch (InvocationTargetException ex) {
            throw new AdapterStartupException(
                    "Exception occured when stopping adapter.", ex);
        }
    }

    public Object getAdaptor() {
        return adapter;
    }

    protected abstract String getClassName();

    protected abstract String getStartMethodName();

    protected abstract String getStopMethodName();

    protected abstract Object[] getConstructorArguments();

    protected abstract Class[] getConstructorArgumentTypes();
    
    protected void preInit(){};
    
    protected void postInit(){};

}