package org.springframework.aop.framework.support;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;

/**
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class AopUtilsTests extends TestCase {

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(AopUtils.getAllInterfaces(testBean));
		assertTrue("Correct number of interfaces", ifcs.size() == 3);
		assertTrue("Contains Serializable", ifcs.contains(Serializable.class));
		assertTrue("Contains ITestBean", ifcs.contains(ITestBean.class));
		assertTrue("Contains IOther", ifcs.contains(IOther.class));
	}

}
