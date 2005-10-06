package org.springframework.aop.interceptor;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public class PerformanceInterceptorTests extends TestCase {

	public void testSuffixAndPrefixAssignment() {
		PerformanceMonitorInterceptor interceptor = new PerformanceMonitorInterceptor();

		assertNotNull(interceptor.getPrefix());
		assertNotNull(interceptor.getSuffix());

		interceptor.setPrefix(null);
		interceptor.setSuffix(null);

		assertNotNull(interceptor.getPrefix());
		assertNotNull(interceptor.getSuffix());
	}
}
