/*
 * Created on Sep 15, 2004
 */
package org.springframework.jmx.adpters.ri;

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