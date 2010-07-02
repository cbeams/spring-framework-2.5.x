
package org.springframework.benchmark;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.load.AbortTestException;
import org.springframework.load.AbstractTest;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 */
public class StaticAopTest extends AbstractTest implements InitializingBean {

	private static String NAME1 = "gary";
	
	private static String NAME2 = "tony";
	
	private static String EMPTY_STR = "";
	
	private ITestBean advised;

	public void afterPropertiesSet() {
		ProxyFactory pf = new ProxyFactory(new Class[] { ITestBean.class });
		pf.setProxyTargetClass(true);

		MethodInterceptor static1 = new Advices.NopInterceptor();

		MethodInterceptor static2 = new Advices.ReadDataInterceptor();

		pf.addAdvice(static1);
		pf.addAdvice(static2);

		Advisor static3 = new Advices.SetterPointCut(new Advices.NopInterceptor());
		Advisor static4 = new Advices.SetterPointCut(new Advices.ReadDataInterceptor());

		pf.addAdvisor(static3);
		pf.addAdvisor(static4);
		
		pf.addAdvisor(new Advices.ObjectReturnPointCut(new Advices.NopInterceptor()));
		
		addFurtherAdvice(pf);

		TestBean target = new TestBean();
		target.setName(NAME1);

		pf.setTarget(target);

		this.advised = (ITestBean) pf.getProxy();
		System.out.println(advised.getClass().getName());
	}
	
	
	/**
	 * These advices won't be tested.
	 */
	protected void addFurtherAdvice(ProxyFactory pf) {
	}

	/**
	 * @see org.springframework.load.AbstractTest#runPass(int)
	 */
	protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		int newAge = 14;
		this.advised.setAge(newAge);
		
		// Nothing on getAge: simulate repeated calls to non-advised methods
		this.advised.getAge();
		this.advised.getAge();
		this.advised.getAge();
		
		if (this.advised.getAge() != newAge)
			throw new TestFailedException("Age should have been " + newAge);
		//System.out.println(this.advised.getName());
		this.advised.setAge(-1);

		this.advised.getName();
		this.advised.setName(NAME2);

		if (!this.advised.getName().equals(NAME2)) {
			throw new TestFailedException("Name should have been tony");
		}

		this.advised.setName(null);
			
		// Interceptor on this
		if (this.advised.returnsThis() != this.advised) {
			throw new TestFailedException("return this should have been magiced");
		}
	}

}
