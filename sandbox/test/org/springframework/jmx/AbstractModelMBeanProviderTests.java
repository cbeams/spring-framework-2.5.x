/*
 * Created on 19-Nov-2004
 */
package org.springframework.jmx;

import javax.management.modelmbean.ModelMBean;

import junit.framework.TestCase;

/**
 * @author robh
 */
public abstract class AbstractModelMBeanProviderTests extends TestCase {

    protected abstract Class getImplementationClass();
    protected abstract ModelMBeanProvider getProvider();
    
    public void testCorrectImplementationClass() throws Exception{
        ModelMBean bean = getProvider().getModelMBean();
        assertTrue(bean.getClass() == getImplementationClass());
    }
    
    public void testInstancesAreDifferent() throws Exception {
        ModelMBean one = getProvider().getModelMBean();
        ModelMBean two = getProvider().getModelMBean();
        assertTrue(one != two);
    }
}
