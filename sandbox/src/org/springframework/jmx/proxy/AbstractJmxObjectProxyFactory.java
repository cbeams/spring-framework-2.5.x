/*
 * Created on Jul 23, 2004
 */
package org.springframework.jmx.proxy;

/**
 * @author robh
 */
public abstract class AbstractJmxObjectProxyFactory implements JmxObjectProxyFactory {

    /**
     * Store the interfaces to proxy
     */
    protected Class[] proxyInterfaces = null;
    
    /**
     * Specfiy with invalid invocations should be ignored or not.
     */
    protected boolean ignoreInvalidInvocations = true;
    

    public boolean getIgnoreInvalidInvocations() {
        return this.ignoreInvalidInvocations;
    }

    public Class[] getProxyInterfaces() {
        return this.proxyInterfaces;
    }

    public void setIgnoreInvalidInvocations(boolean ignoreInvalidInvocatios) {
      this.ignoreInvalidInvocations = ignoreInvalidInvocatios;
    }

    public void setProxyInterfaces(Class[] interfaces) {
        this.proxyInterfaces = interfaces;
    }
}
