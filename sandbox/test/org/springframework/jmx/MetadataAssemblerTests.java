/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.jmx.assemblers.metadata.MetadataModelMBeanInfoAssembler;

/**
 * @author robh
 */
public class MetadataAssemblerTests extends AbstractJmxAssemblerTests {

    private static final String OBJECT_NAME = "bean:name=testBean3";

    public MetadataAssemblerTests(String name) {
        super(name);
    }

    public void testDescription() throws Exception {
        MBeanInfo info = getMBeanInfo();
        assertEquals("The descriptions are not the same", "My Managed Bean",
                info.getDescription());
    }

    public void testAttributeDescriptionOnSetter() throws Exception {
        ModelMBeanInfo inf = getMBeanInfoFromAssembler();

        ModelMBeanAttributeInfo attr = inf.getAttribute("age");

        assertEquals("The description for the age attribute is incorrect",
                "The Age Attribute", attr.getDescription());
    }

    public void testAttributeDescriptionOnGetter() throws Exception {
        ModelMBeanInfo inf = getMBeanInfoFromAssembler();

        ModelMBeanAttributeInfo attr = inf.getAttribute("name");

        assertEquals("The description for the name attribute is incorrect",
                "The Name Attribute", attr.getDescription());
    }

    public void testReadOnlyAttribute() throws Exception {
        ModelMBeanInfo inf = getMBeanInfoFromAssembler();

        ModelMBeanAttributeInfo attr = inf.getAttribute("age");

        assertFalse("The age attribute should not be writable",
                attr.isWritable());
    }

    public void testReadWriteAttribute() throws Exception {
        ModelMBeanInfo inf = getMBeanInfoFromAssembler();

        ModelMBeanAttributeInfo attr = inf.getAttribute("name");

        assertTrue("The name attribute should be writable",
                attr.isWritable());
        assertTrue("The name attribute should be readable",
                attr.isReadable());
    }

    protected String getObjectName() {
        return OBJECT_NAME;
    }

    protected int getExpectedAttributeCount() {
        return 2;
    }

    protected int getExpectedOperationCount() {
        return 2;
    }

    protected ModelMBeanInfoAssembler getAssembler() {
        return new MetadataModelMBeanInfoAssembler();
    }
}