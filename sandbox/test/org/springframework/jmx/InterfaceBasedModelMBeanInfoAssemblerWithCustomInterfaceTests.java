package org.springframework.jmx;

import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.reflection.InterfaceBasedModelMBeanInfoAssembler;

import javax.management.Attribute;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;

/**
 * @author robh
 */
public class InterfaceBasedModelMBeanInfoAssemblerWithCustomInterfaceTests extends AbstractJmxAssemblerTests {

    protected static final String OBJECT_NAME = "bean:name=testBean5";

    public InterfaceBasedModelMBeanInfoAssemblerWithCustomInterfaceTests(String name) {
        super(name);
    }

    protected String getObjectName() {
        return OBJECT_NAME;
    }

    protected int getExpectedOperationCount() {
        return 5;
    }

    protected int getExpectedAttributeCount() {
        return 2;
    }

    protected ModelMBeanInfoAssembler getAssembler() {
        InterfaceBasedModelMBeanInfoAssembler assembler = new InterfaceBasedModelMBeanInfoAssembler();
        assembler.setManagedInterfaces(new Class[]{ICustomJmxBean.class});
        return assembler;
    }

    public void testGetAgeIsReadOnly() throws Exception {
        ModelMBeanInfo info = getMBeanInfoFromAssembler();
        ModelMBeanAttributeInfo attr = info.getAttribute("age");

        assertTrue(attr.isReadable());
        assertFalse(attr.isWritable());
    }
}
