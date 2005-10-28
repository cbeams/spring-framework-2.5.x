package org.springframework.aop.support.aspectj;

import junit.framework.TestCase;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.UnsupportedPointcutPrimitiveException;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.TestBean;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class AspectJExpressionPointcutTests extends TestCase {

	private Method getAge;

	private Method setAge;

	private Method setSomeNumber;

	public void testMatchExplicit() {
		String expression = "execution(int org.springframework.beans.TestBean.getAge())";

		Pointcut pointcut = getPointcut(expression);
		ClassFilter classFilter = pointcut.getClassFilter();
		MethodMatcher methodMatcher = pointcut.getMethodMatcher();

		assertMatchesTestBeanClass(classFilter);

		// not currently testable in a reliable fashion
		//assertDoesNotMatchStringClass(classFilter);

		assertFalse("Should not be a runtime match", methodMatcher.isRuntime());
		assertMatchesGetAge(methodMatcher);
		assertFalse("Expression should match setAge() method", methodMatcher.matches(setAge, TestBean.class));
	}


	public void setUp() throws NoSuchMethodException {
		getAge = TestBean.class.getMethod("getAge", null);
		setAge = TestBean.class.getMethod("setAge", new Class[]{int.class});
		setSomeNumber = TestBean.class.getMethod("setSomeNumber", new Class[]{Number.class});
	}

	public void testMatchWithTypePattern() throws Exception {
		String expression = "execution(* *..TestBean.*Age(..))";

		Pointcut pointcut = getPointcut(expression);
		ClassFilter classFilter = pointcut.getClassFilter();
		MethodMatcher methodMatcher = pointcut.getMethodMatcher();

		assertMatchesTestBeanClass(classFilter);

		// not currently testable in a reliable fashion
		//assertDoesNotMatchStringClass(classFilter);

		assertFalse("Should not be a runtime match", methodMatcher.isRuntime());
		assertMatchesGetAge(methodMatcher);
		assertTrue("Expression should match setAge(int) method", methodMatcher.matches(setAge, TestBean.class));
	}

	public void testMatchWithArgs() throws Exception {
		String expression = "execution(void org.springframework.beans.TestBean.setSomeNumber(Number)) && args(Double)";

		Pointcut pointcut = getPointcut(expression);
		ClassFilter classFilter = pointcut.getClassFilter();
		MethodMatcher methodMatcher = pointcut.getMethodMatcher();

		assertMatchesTestBeanClass(classFilter);

		// not currently testable in a reliable fashion
		//assertDoesNotMatchStringClass(classFilter);

		assertTrue("Should match with setSomeNumber with Double input",
				methodMatcher.matches(setSomeNumber, TestBean.class, new Object[]{new Double(12)}));
		assertFalse("Should not match setSomeNumber with Integer input",
				methodMatcher.matches(setSomeNumber, TestBean.class, new Object[]{new Integer(11)}));
		assertFalse("Should not match getAge", methodMatcher.matches(getAge, TestBean.class, null));
		assertTrue("Should be a runtime match", methodMatcher.isRuntime());
	}

	public void testSimpleAdvice() {
		String expression = "execution(int org.springframework.beans.TestBean.getAge())";

		CallCountingInterceptor interceptor = new CallCountingInterceptor();

		TestBean testBean = getAdvisedProxy(expression, interceptor);

		assertEquals("Calls should be 0", 0, interceptor.getCount());

		testBean.getAge();

		assertEquals("Calls should be 1", 1, interceptor.getCount());

		testBean.setAge(90);

		assertEquals("Calls should still be 1", 1, interceptor.getCount());
	}

	public void testDynamicMatchingProxy() {
		String expression = "execution(void org.springframework.beans.TestBean.setSomeNumber(Number)) && args(Double)";

		CallCountingInterceptor interceptor = new CallCountingInterceptor();

		TestBean testBean = getAdvisedProxy(expression, interceptor);

		assertEquals("Calls should be 0", 0, interceptor.getCount());

		testBean.setSomeNumber(new Double(30));

		assertEquals("Calls should be 1", 1, interceptor.getCount());

		testBean.setSomeNumber(new Integer(90));

		assertEquals("Calls should be 1", 1, interceptor.getCount());
	}

	public void testInvalidExpression() {
		String expression = "execution(void org.springframework.beans.TestBean.setSomeNumber(Number) && args(Double)";

		try {
			getPointcut(expression);
			fail("Invalid expression should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			assertTrue(true);
			System.out.println(ex.getMessage());
		}
	}

	private TestBean getAdvisedProxy(String pointcutExpression, CallCountingInterceptor interceptor) {
		TestBean target = new TestBean();

		Pointcut pointcut = getPointcut(pointcutExpression);

		DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
		advisor.setAdvice(interceptor);
		advisor.setPointcut(pointcut);

		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(target);
		pf.addAdvisor(advisor);

		return (TestBean) pf.getProxy();
	}

	private void assertMatchesGetAge(MethodMatcher methodMatcher) {
		assertTrue("Expression should match getAge() method", methodMatcher.matches(getAge, TestBean.class));
	}

	private void assertMatchesTestBeanClass(ClassFilter classFilter) {
		assertTrue("Expression should match TestBean class", classFilter.matches(TestBean.class));
	}

	private void assertDoesNotMatchStringClass(ClassFilter classFilter) {
		assertFalse("Expression should not match String class", classFilter.matches(String.class));
	}

	public void testWithUnsupportedPointcutPrimitive() throws Exception {
		String expression = "call(int org.springframework.beans.TestBean.getAge())";

		try {
			getPointcut(expression);
			fail("Should not support call pointcuts");
		}
		catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support call pointcut", PointcutPrimitive.CALL, ex.getUnsupportedPrimitive());
		}

	}

	private Pointcut getPointcut(String expression) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression(expression);
		return pointcut;
	}
}
