/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx;

import javax.management.Descriptor;
import javax.management.MBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.metadata.MetadataModelMBeanInfoAssembler;
import org.springframework.jmx.metadata.support.JmxAttributeSource;

/**
 * @author robh
 */
public abstract class AbstractMetadataAssemblerTests extends AbstractJmxAssemblerTests {

    private static final String OBJECT_NAME = "bean:name=testBean3";

    public AbstractMetadataAssemblerTests(String name) {
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

    /**
     * Tests the situation where the attribute is 
     * only defined on the getter
     * @throws Exception
     */
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
    
    /**
     * Tests the situation where the property only has 
     * a getter
     * @throws Exception
     */
    public void testWithOnlyGetter() throws Exception {
        ModelMBeanInfo inf = getMBeanInfoFromAssembler();
        
        ModelMBeanAttributeInfo attr = inf.getAttribute("nickName");
        
        assertNotNull("Attribute should not be null", attr);
    }
    
    /**
     * Tests the situation where the property only
     * has a setter
     * @throws Exception
     */
    public void testWithOnlySetter() throws Exception {
        ModelMBeanInfo info = getMBeanInfoFromAssembler();
        
        ModelMBeanAttributeInfo attr = info.getAttribute("superman");
        
        assertNotNull("Attribute should not be null", attr);
    }

    public void testManagedResourceDescriptor() throws Exception {
        ModelMBeanInfo info = getMBeanInfoFromAssembler();
        
        Descriptor desc = info.getMBeanDescriptor();
        
        assertEquals("Logging should be set to true", "true", desc.getFieldValue("log"));
        assertEquals("Log file should be jmx.log", "jmx.log", desc.getFieldValue("logFile"));
        assertEquals("Currency Time Limit should be 15", new Integer(15), desc.getFieldValue("currencyTimeLimit"));
        assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
        assertEquals("Persist Period should be 200", new Integer(200), desc.getFieldValue("persistPeriod"));
        assertEquals("Persist Location should be foo", "foo", desc.getFieldValue("persistLocation"));
        assertEquals("Persist Name should be bar", "bar", desc.getFieldValue("persistName"));
    }
    
    public void testAttributeDescriptor() throws Exception {
        ModelMBeanInfo info = getMBeanInfoFromAssembler();
        
        Descriptor desc = info.getAttribute("name").getDescriptor();
        
        assertEquals("Default value should be foo", "foo", desc.getFieldValue("default"));
        assertEquals("Currency Time Limit should be 20", new Integer(20), desc.getFieldValue("currencyTimeLimit"));
        assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
        assertEquals("Persist Period should be 300", new Integer(300), desc.getFieldValue("persistPeriod"));
    }
    
    public void testOperationDescriptor() throws Exception {
        ModelMBeanInfo info = getMBeanInfoFromAssembler();
        
        Descriptor desc = info.getOperation("myOperation").getDescriptor();
        
        assertEquals("Currency Time Limit should be 30", new Integer(30), desc.getFieldValue("currencyTimeLimit"));
        assertEquals("Role should be \"operation\"", "operation", desc.getFieldValue("role"));
    }
    
    protected String getObjectName() {
        return OBJECT_NAME;
    }

    protected int getExpectedAttributeCount() {
        return 4;
    }

    protected int getExpectedOperationCount() {
        return 7;
    }

    protected ModelMBeanInfoAssembler getAssembler() {
        MetadataModelMBeanInfoAssembler assembler = new MetadataModelMBeanInfoAssembler();
        assembler.setAttributeSource(getAttributeSource());
        return assembler;
    }
    
    protected abstract JmxAttributeSource getAttributeSource();
}