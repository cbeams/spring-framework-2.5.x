/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.TestBean;

/**
 * TODO COULD REFACTOR TO BE GENERIC
 * @author Rod Johnson
 * @since 14-Mar-2003
 * @version $Revision: 1.6 $
 */
public class MethodInvocationTests extends TestCase {
	
	/*
	public static MethodInvocation testInvocation(Object o, String methodName, Class[] args, Interceptor[] interceptors) throws Exception {
		Method m = o.getClass().getMethod(methodName, args);
		MethodInvocationImpl invocation = new MethodInvocationImpl(null, null, m.getDeclaringClass(), 
	m, null, interceptors, // list
new Attrib4jAttributeRegistry());
	return invocation;
}*/

	/**
	 * Constructor for MethodInvocationTests.
	 * @param arg0
	 */
	public MethodInvocationTests(String arg0) {
		super(arg0);
	}

/*
	public void testNullInterceptor() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		try {
				MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null, null // could customize here
	);
			fail("Shouldn't be able to create methodInvocationImpl with null interceptors");
		} catch (AopConfigException ex) {
		}
	}

	public void testEmptyInterceptorList() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		try {
				MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null, new LinkedList() // list
	);
			fail("Shouldn't be able to create methodInvocationImpl with no interceptors");
		} catch (AopConfigException ex) {
		}
	}
*/

	public void testValidInvocation() throws Throwable {
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		final Object returnValue = new Object();
		List is = new LinkedList();
		is.add(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				return returnValue;
			}
		});
			MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null, null, is // list
	);
		Object rv = invocation.proceed();
		assertTrue("correct response", rv == returnValue);
	}
	

	public void testLimits() throws Throwable {
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		final Object returnValue = new Object();
		List is = new LinkedList();
		MethodInterceptor interceptor = new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				return returnValue;
			}
		};
		is.add(interceptor);

			MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null, null, is // list
	);
		assertTrue(invocation.getArgumentCount() == 0);
		//assertTrue(invocation.getCurrentInterceptorIndex() == 0);
		//assertTrue(invocation.getInterceptor(0) == interceptor);
		Object rv = invocation.proceed();
		assertTrue("correct response", rv == returnValue);

		//assertTrue(invocation.getCurrentInterceptorIndex() == 0);
		//assertTrue(invocation.getNumberOfInterceptors() == 1);

		// Now it gets interesting
		try {
			invocation.proceed();
			fail("Shouldn't allow illegal invocation number");
		} catch (AspectException ex) {
			// Shouldn't have changed position in interceptor chain
			//assertTrue(
			//	"Shouldn't have changed current interceptor index",
			//	invocation.getCurrentInterceptorIndex() == 0);
		}

//		try {
//			invocation.getInterceptor(666);
//			fail("Shouldn't allow illegal interceptor get");
//		} catch (AspectException ex) {
//		}
	}

	public void testAttachments() throws Throwable {
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		final Object returnValue = new Object();
		List is = new LinkedList();
		is.add(new DefaultInterceptionAroundAdvisor(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				return returnValue;
			}
		}));

			MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null, null, is // list
	);

		assertTrue("no bogus attachment", invocation.getAttachment("bogus") == null);
		String name = "foo";
		Object val = new Object();
		Object val2 = "foo";
		assertTrue("New attachment returns null", null == invocation.addAttachment(name, val));
		assertTrue(invocation.getAttachment(name) == val);
		assertTrue("Replace returns correct value", val == invocation.addAttachment(name, val2));
		assertTrue(invocation.getAttachment(name) == val2);
		assertTrue("Can clear by attaching null", val2 == invocation.addAttachment(name, null));
	}

	/**
	 * ToString on target can cause failure
	 * @throws Throwable
	 */
	public void testToStringDoesntHitTarget() throws Throwable {
		Object target = new TestBean() {
			public String toString() {
				throw new UnsupportedOperationException("toString");
			}
		};
		final Object returnValue = new Object();
		List is = new LinkedList();
		is.add(new DefaultInterceptionAroundAdvisor(new InvokerInterceptor(target)));

		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
			MethodInvocationImpl invocation = new MethodInvocationImpl(proxy, target, m.getDeclaringClass(), //?
		m, null, null, is // list
	);

		// if it hits target the test will fail with the UnsupportedOpException
		// in the inner class above
		invocation.toString();
	}
}