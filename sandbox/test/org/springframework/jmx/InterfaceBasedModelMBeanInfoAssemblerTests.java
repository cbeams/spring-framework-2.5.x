package org.springframework.jmx;

import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.reflection.InterfaceBasedModelMBeanInfoAssembler;

/**
 * @author robh
 */
public class InterfaceBasedModelMBeanInfoAssemblerTests extends AbstractJmxAssemblerTests {

    protected static final String OBJECT_NAME = "bean:name=testBean4";

    public InterfaceBasedModelMBeanInfoAssemblerTests(String name) {
        super(name);
    }

    protected String getObjectName() {
        return OBJECT_NAME;
    }

    protected int getExpectedOperationCount() {
        return 7;
    }

    protected int getExpectedAttributeCount() {
        return 2;
    }

    protected ModelMBeanInfoAssembler getAssembler() {
        return new InterfaceBasedModelMBeanInfoAssembler();
    }

    protected String getApplicationContextPath() {
        return "org/springframework/jmx/interfaceAssembler.xml";
    }

}
