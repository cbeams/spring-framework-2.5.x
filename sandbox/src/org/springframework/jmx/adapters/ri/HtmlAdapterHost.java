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
package org.springframework.jmx.adapters.ri;

import org.springframework.jmx.adapters.AbstractReflectionBasedAdapterHost;

/**
 * @author robh
 */
public class HtmlAdapterHost extends AbstractReflectionBasedAdapterHost {

    private static final String CLASS_NAME = "com.sun.jdmk.comm.HtmlAdaptorServer";

    private static final String START_METHOD_NAME = "start";
    
    private static final String STOP_METHOD_NAME = "stop";

    private int port = 9090;
    
    public void setPort(int port) {
        this.port = port;
    }
    
    protected String getClassName() {
        return CLASS_NAME;
    }

    protected String getStartMethodName() {
        return START_METHOD_NAME;
    }
    
    protected String getStopMethodName() {
        return STOP_METHOD_NAME;
    }

    protected Object[] getConstructorArguments() {
        return new Object[] { new Integer(port) };
    }

    protected Class[] getConstructorArgumentTypes() {
        return new Class[] { int.class };
    }

}