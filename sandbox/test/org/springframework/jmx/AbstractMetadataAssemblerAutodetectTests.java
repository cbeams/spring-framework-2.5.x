/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.metadata.MetadataModelMBeanInfoAssembler;
import org.springframework.jmx.metadata.support.JmxAttributeSource;

/**
 * @author robh
 */
public abstract class AbstractMetadataAssemblerAutodetectTests extends AbstractAutodetectTest {

    public AbstractMetadataAssemblerAutodetectTests(String name) {
        super(name);
    }
        
    protected AutodetectCapableModelMBeanInfoAssembler getAssembler() {
        MetadataModelMBeanInfoAssembler assembler = new MetadataModelMBeanInfoAssembler();
        assembler.setAttributeSource(getAttributeSource());
        return assembler;
    }
    
    protected abstract JmxAttributeSource getAttributeSource();

}
