/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

/**
 * @author robh
 */
public interface AutodetectCapableModelMBeanInfoAssembler extends
        ModelMBeanInfoAssembler {

    public boolean includeBean(String beanName, Object bean);
}
