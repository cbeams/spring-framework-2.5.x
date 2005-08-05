package org.springframework.aop.target;

import junit.framework.TestCase;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Rob Harrop
 */
public class AbstractLazyInitTargetSourceTests extends TestCase {

	public void testCreateLazy() {
		TargetSource targetSource = new AbstractLazyInitTargetSource() {
			protected Object createObject() throws Exception {
				return new InitCountingBean();
			}

			public Class getTargetClass() {
				return InitCountingBean.class;
			}
		};

		InitCountingBean proxy = (InitCountingBean) ProxyFactory.getProxy(targetSource);

		assertEquals("Init count should be 0", 0, InitCountingBean.initCount);
		assertEquals("Target class incorrect", InitCountingBean.class, targetSource.getTargetClass());
		assertEquals("Init count should still be 0 after getTargetClass()", 0, InitCountingBean.initCount);

		proxy.doSomething();

		assertEquals("Init count should now be 1", 1, InitCountingBean.initCount);


		proxy.doSomething();
		                          
		assertEquals("Init count should still be 1", 1, InitCountingBean.initCount);
	}

	private static class InitCountingBean {

		public static int initCount;

		public InitCountingBean() {
			if (InitCountingBean.class.equals(getClass())) {
				// only increment when creating the actual target - not the proxy
				initCount++;
			}
		}

		public void doSomething() {
			//no-op
		}
	}

}
