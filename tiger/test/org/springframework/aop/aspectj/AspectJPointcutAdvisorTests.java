package org.springframework.aop.aspectj;

import junit.framework.TestCase;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectMetadata;
import org.springframework.aop.aspectj.AtAspectJAdvisorFactory;
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.ReflectiveAtAspectJAdvisorFactory;
import org.springframework.aop.aspectj.SingletonMetadataAwareAspectInstanceFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.aspectj.AspectJExpressionPointcutTests;
import org.springframework.beans.TestBean;

/**
 * @author Rod Johnson 
 */
public class AspectJPointcutAdvisorTests extends TestCase {
	
	private AtAspectJAdvisorFactory af = new ReflectiveAtAspectJAdvisorFactory();

	public void testSingleton() throws SecurityException, NoSuchMethodException {
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
		ajexp.setExpression(AspectJExpressionPointcutTests.MATCH_ALL_METHODS);
		
		InstantiationModelAwarePointcutAdvisor ajpa = new InstantiationModelAwarePointcutAdvisor(af, ajexp, 
				new SingletonMetadataAwareAspectInstanceFactory(new AbstractAtAspectJAdvisorFactoryTests.ExceptionAspect(null)), 
				TestBean.class.getMethod("getAge", (Class[]) null));
		assertSame(Pointcut.TRUE, ajpa.getAspectMetadata().getPerClausePointcut());
		assertFalse(ajpa.isPerInstance());
	}
	
	public void testPerTarget() throws SecurityException, NoSuchMethodException {
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
		ajexp.setExpression(AspectJExpressionPointcutTests.MATCH_ALL_METHODS);
		
		InstantiationModelAwarePointcutAdvisor ajpa = new InstantiationModelAwarePointcutAdvisor(af, ajexp, 
				new SingletonMetadataAwareAspectInstanceFactory(new AbstractAtAspectJAdvisorFactoryTests.PerTargetAspect()), null);
		assertNotSame(Pointcut.TRUE, ajpa.getAspectMetadata().getPerClausePointcut());
		assertTrue(ajpa.getAspectMetadata().getPerClausePointcut() instanceof AspectJExpressionPointcut);
		assertTrue(ajpa.isPerInstance());
		
		assertTrue(ajpa.getAspectMetadata().getPerClausePointcut().getClassFilter().matches(TestBean.class));
		assertFalse(ajpa.getAspectMetadata().getPerClausePointcut().getMethodMatcher().matches(
				TestBean.class.getMethod("getAge", (Class[]) null),
				TestBean.class));
		
		assertTrue(ajpa.getAspectMetadata().getPerClausePointcut().getMethodMatcher().matches(
				TestBean.class.getMethod("getSpouse", (Class[]) null),
				TestBean.class));
	}
	
	
	public void testPerCflowTarget() {
		testIllegalInstantiationModel(AbstractAtAspectJAdvisorFactoryTests.PerCflowAspect.class);
	}
	
	public void testPerCflowBelowTarget() {
		testIllegalInstantiationModel(AbstractAtAspectJAdvisorFactoryTests.PerCflowBelowAspect.class);
	}
	
	private void testIllegalInstantiationModel(Class c) {
		try {
			new AspectMetadata(c);
			fail();
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

}
