
package org.springframework.jmx.assembler;

import java.util.Properties;

import javax.management.MBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.context.ApplicationContextException;

/**
 * @author robh
 */
public class InterfaceBasedModelMBeanInfoAssemblerWithMappedInterfaceTests extends AbstractJmxAssemblerTests {

	protected static final String OBJECT_NAME = "bean:name=testBean4";

	public void testGetAgeIsReadOnly() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = info.getAttribute("age");

		assertTrue("Age is not readable", attr.isReadable());
		assertFalse("Age is not writable", attr.isWritable());
	}

	public void testWithUnknownClass() throws Exception {
		InterfaceBasedModelMBeanInfoAssembler assembler = getWithMapping("com.foo.bar.Unknown");

		assertThrowsApplicationContextException(assembler);
	}

	public void testWithNonInterface() throws Exception {
		InterfaceBasedModelMBeanInfoAssembler assembler = getWithMapping("org.springframework.jmx.JmxTestBean");

		assertThrowsApplicationContextException(assembler);
	}

	public void testWithFallThrough() throws Exception {
		InterfaceBasedModelMBeanInfoAssembler assembler = getWithMapping("foobar", "org.springframework.jmx.ICustomJmxBean");
		assembler.setManagedInterfaces(new Class[]{IAdditionalTestMethods.class});
		assembler.afterPropertiesSet();

		ModelMBeanInfo inf = assembler.getMBeanInfo(getObjectName(), getBean().getClass());
		MBeanAttributeInfo attr = inf.getAttribute("nickName");

		assertNickName(attr);
	}

	public void testNickNameIsExposed() throws Exception {
		ModelMBeanInfo inf = (ModelMBeanInfo) getMBeanInfo();
		MBeanAttributeInfo attr = inf.getAttribute("nickName");

		assertNickName(attr);
	}

	protected String getObjectName() {
		return OBJECT_NAME;
	}

	protected int getExpectedOperationCount() {
		return 7;
	}

	protected int getExpectedAttributeCount() {
		return 3;
	}

	protected ModelMBeanInfoAssembler getAssembler() throws Exception {
		InterfaceBasedModelMBeanInfoAssembler assembler = getWithMapping("org.springframework.jmx.assembler.IAdditionalTestMethods, org.springframework.jmx.ICustomJmxBean");
		assembler.afterPropertiesSet();
		return assembler;
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/assembler/interfaceAssemblerMapped.xml";
	}

	private InterfaceBasedModelMBeanInfoAssembler getWithMapping(String mapping) {
		return getWithMapping(OBJECT_NAME, mapping);
	}

	private InterfaceBasedModelMBeanInfoAssembler getWithMapping(String name, String mapping) {
		InterfaceBasedModelMBeanInfoAssembler assembler = new InterfaceBasedModelMBeanInfoAssembler();
		Properties props = new Properties();
		props.setProperty(name, mapping);
		assembler.setMappings(props);

		return assembler;
	}

	private void assertNickName(MBeanAttributeInfo attr) {
		assertNotNull("Nick Name should not be null", attr);
		assertTrue("Nick Name should be writable", attr.isWritable());
		assertTrue("Nick Name should be readab;e", attr.isReadable());
	}

	private void assertThrowsApplicationContextException(InterfaceBasedModelMBeanInfoAssembler assembler) throws Exception {
		try {
			assembler.afterPropertiesSet();
			fail("Should cause exception");
		}
		catch (ApplicationContextException ex) {
			assertTrue(true);
		}
	}
}
