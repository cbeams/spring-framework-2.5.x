
package org.springframework.jmx;

import javax.management.DynamicMBean;
import javax.management.StandardMBean;
import javax.management.NotCompliantMBeanException;

import junit.framework.TestCase;

import org.springframework.jmx.util.JmxUtils;

/**
 * @author robh
 */
public class JmxUtilsTests extends TestCase {

		public void testIsMBeanWithDynamicMBean() throws Exception {
			DynamicMBean mbean = new DynamicMBeanTest();

			assertTrue("Dynamic MBean not detected correctly", JmxUtils.isMBean(mbean));
		}

	public void testIsMBeanWithStandardMBeanWrapper() throws Exception {
		StandardMBean mbean = new StandardMBean(new JmxTestBean(),IJmxTestBean.class);

		assertTrue("Standard MBean not detected correctly", JmxUtils.isMBean(mbean));
	}

	public void testIsMBeanWithStandardMBeanInherited() throws Exception {
    StandardMBean mbean = new StandardMBeanImpl();

		assertTrue("Standard MBean not detected correctly", JmxUtils.isMBean(mbean));
	}

	public void testNotAnMBean() throws Exception {
		assertFalse("Object incorrectly identified as an MBean", JmxUtils.isMBean(new Object()));
	}

	public void testSimpleMBean() throws Exception {
     Foo foo = new Foo();

		assertTrue("Simple MBean not detected correctly", JmxUtils.isMBean(foo));
	}

	public void testSimpleMBeanThroughInheritance() throws Exception {
     Bar bar = new Bar();
     Abc abc = new Abc();

		assertTrue("Simple MBean (through inheritance) not detected correctly", JmxUtils.isMBean(bar));
		assertTrue("Simple MBean (through 2 levels of inheritance) not detected correctly", JmxUtils.isMBean(abc));
	}

	public static class StandardMBeanImpl extends StandardMBean implements IJmxTestBean {

		public StandardMBeanImpl() throws NotCompliantMBeanException {
			super(IJmxTestBean.class);
		}

		public int add(int x, int y) {
			return 0;
		}

		public long myOperation() {
			return 0;
		}

		public int getAge() {
			return 0;
		}

		public void setAge(int age) {

		}

		public void setName(String name) {

		}

		public String getName() {
			return null;
		}

		public void dontExposeMe() {

		}
	}

	public static interface FooMBean {
		String getName();
	}

	public static class Foo implements FooMBean {
		public String getName() {
			return "Rob Harrop";
		}
	}

	public static class Bar extends Foo {

	}

	public static class Abc extends Bar {

	}
}
