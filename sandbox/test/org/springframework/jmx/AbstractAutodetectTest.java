/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.metadata.MetadataModelMBeanInfoAssembler;


/**
 * @author robh
 */
public abstract class AbstractAutodetectTest extends AbstractJmxTests {


    public AbstractAutodetectTest(String name) {
        super(name);
    }
    
    public void testAutodetect() throws Exception {
        JmxTestBean bean = new JmxTestBean();
       
        AutodetectCapableModelMBeanInfoAssembler assembler = getAssembler();
        assertTrue("The bean should be included", assembler.includeBean("testBean", bean));
        
    }  
    
    protected abstract AutodetectCapableModelMBeanInfoAssembler getAssembler();
}
