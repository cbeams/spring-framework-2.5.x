
package org.springframework.aop.aspectj;

import org.springframework.beans.ITestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author robh
 */
public class AspectJExpressionPointcutAdvisorTests extends AbstractDependencyInjectionSpringContextTests {

	private ITestBean testBean;

	private CallCountingInterceptor interceptor;

	public void setTestBean(ITestBean testBean) {
		this.testBean = testBean;
	}

	public void setInterceptor(CallCountingInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	protected void onSetUp() throws Exception {
		interceptor.reset();
	}

	public void testPointcutting() throws Exception {
		assertEquals("Count should be 0", 0, interceptor.getCount());
		testBean.getAge();
		assertEquals("Count should be 1", 1, interceptor.getCount());
		testBean.setAge(90);
		assertEquals("Count should be 1", 1, interceptor.getCount());
	}

	protected String[] getConfigLocations() {
		return new String[]{"org/springframework/aop/aspectj/aspectj.xml"};
	}
}
