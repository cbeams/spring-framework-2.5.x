package org.springframework.jmx.export.assembler;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.MBeanAttributeInfo;
import java.util.Properties;

/**
 * @author Rob Harrop
 */
public class MethodExclusionMBeanInfoAssemblerMappedTests extends AbstractJmxAssemblerTests {

	protected static final String OBJECT_NAME = "bean:name=testBean4";

	public void testGetAgeIsReadOnly() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = info.getAttribute(AGE_ATTRIBUTE);

		assertTrue("Age is not readable", attr.isReadable());
		assertFalse("Age is not writable", attr.isWritable());
	}

	public void testNickNameIsExposed() throws Exception {
		ModelMBeanInfo inf = (ModelMBeanInfo) getMBeanInfo();
		MBeanAttributeInfo attr = inf.getAttribute("NickName");

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

	protected MBeanInfoAssembler getAssembler() throws Exception {
		return getWithMapping("setAge,isSuperman,setSuperman,dontExposeMe");
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/export/assembler/methodExclusionAssemblerMapped.xml";
	}

	private MethodExclusionMBeanInfoAssembler getWithMapping(String mapping) {
		return getWithMapping(OBJECT_NAME, mapping);
	}

	private MethodExclusionMBeanInfoAssembler getWithMapping(String name, String mapping) {
		MethodExclusionMBeanInfoAssembler assembler = new MethodExclusionMBeanInfoAssembler();
		Properties props = new Properties();
		props.setProperty(name, mapping);
		assembler.setIgnoredMethodMappings(props);
		return assembler;
	}

	private void assertNickName(MBeanAttributeInfo attr) {
		assertNotNull("Nick Name should not be null", attr);
		assertTrue("Nick Name should be writable", attr.isWritable());
		assertTrue("Nick Name should be readable", attr.isReadable());
	}

}
