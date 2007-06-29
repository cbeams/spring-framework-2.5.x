package org.springframework.aop.aspectj.generic;

import java.util.ArrayList;
import java.util.Collection;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.Employee;
import org.springframework.beans.TestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests ensuring that after-returning advice for generic parameters bound to
 * the advice and the return type follow AspectJ semantics.
 * <p>
 * See SPR-3628 for more details.
 * 
 * @author Ramnivas Laddad
 * 
 */
public class AfterReturningGenericTypeMatchingTests extends AbstractDependencyInjectionSpringContextTests {
	protected GenericReturnTypeVariationClass testBean;

	protected CounterAspect counterAspect;

	public AfterReturningGenericTypeMatchingTests() {
		setPopulateProtectedVariables(true);
	}

	@Override
	protected String getConfigPath() {
		return "afterReturningGenericTypeMatchingTests-context.xml";
	}

	@Override
	protected void onSetUp() throws Exception {
		counterAspect.reset();
		super.onSetUp();
	}

	public void testReturnTypeExactMatching() {
		testBean.getStrings();
		assertEquals(1, counterAspect.getStringsInvocationsCount);
		assertEquals(0, counterAspect.getIntegersInvocationsCount);

		counterAspect.reset();

		testBean.getIntegers();
		assertEquals(0, counterAspect.getStringsInvocationsCount);
		assertEquals(1, counterAspect.getIntegersInvocationsCount);
	}

	public void testReturnTypeRawMatching() {
		testBean.getStrings();
		assertEquals(1, counterAspect.getRawsInvocationsCount);

		counterAspect.reset();

		testBean.getIntegers();
		assertEquals(1, counterAspect.getRawsInvocationsCount);
	}

	public void testReturnTypeUpperBoundeMatching() {
		testBean.getIntegers();
		assertEquals(1, counterAspect.getNumbersInvocationsCount);
	}

	public void testReturnTypeLowerBoundeMatching() {
		testBean.getTestBeans();
		assertEquals(1, counterAspect.getTestBeanInvocationsCount);

		counterAspect.reset();

		testBean.getEmployees();
		assertEquals(0, counterAspect.getTestBeanInvocationsCount);
	}

	public static class GenericReturnTypeVariationClass {
		public Collection<String> getStrings() {
			return new ArrayList<String>();
		}

		public Collection<Integer> getIntegers() {
			return new ArrayList<Integer>();
		}

		public Collection<TestBean> getTestBeans() {
			return new ArrayList<TestBean>();
		}

		public Collection<Employee> getEmployees() {
			return new ArrayList<Employee>();
		}
	}

	@Aspect
	public static class CounterAspect {
		private int getRawsInvocationsCount;

		private int getStringsInvocationsCount;

		private int getIntegersInvocationsCount;

		private int getNumbersInvocationsCount;

		private int getTestBeanInvocationsCount;

		@Pointcut("execution(* org.springframework.aop.aspectj.generic.AfterReturningGenericTypeMatchingTests.GenericReturnTypeVariationClass.*(..))")
		public void anyTestMethod() {
		}

		@AfterReturning(pointcut = "anyTestMethod()", returning = "ret")
		public void incrementGetRawsInvocationsCount(Collection ret) {
			getRawsInvocationsCount++;
		}

		@AfterReturning(pointcut = "anyTestMethod()", returning = "ret")
		public void incrementGetStringsInvocationsCount(Collection<String> ret) {
			getStringsInvocationsCount++;
		}

		@AfterReturning(pointcut = "anyTestMethod()", returning = "ret")
		public void incrementGetIntegersInvocationsCount(Collection<Integer> ret) {
			getIntegersInvocationsCount++;
		}

		@AfterReturning(pointcut = "anyTestMethod()", returning = "ret")
		public void incrementGetNumbersInvocationsCount(Collection<? extends Number> ret) {
			getNumbersInvocationsCount++;
		}

		@AfterReturning(pointcut = "anyTestMethod()", returning = "ret")
		public void incrementTestBeanInvocationsCount(Collection<? super TestBean> ret) {
			getTestBeanInvocationsCount++;
		}

		public void reset() {
			getRawsInvocationsCount = 0;
			getStringsInvocationsCount = 0;
			getIntegersInvocationsCount = 0;
			getNumbersInvocationsCount = 0;
			getTestBeanInvocationsCount = 0;
		}
	}
}