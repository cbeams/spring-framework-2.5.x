/*
 * Created on 19-Nov-2004
 */
package org.springframework.jmx;

import javax.management.modelmbean.RequiredModelMBean;

/**
 * @author robh
 */
public class RequiredModelMBeanProviderTests extends AbstractModelMBeanProviderTests {


    protected Class getImplementationClass() {
        return RequiredModelMBean.class;
    }


    protected ModelMBeanProvider getProvider() {
        return new RequiredModelMBeanProvider();
    }

}
