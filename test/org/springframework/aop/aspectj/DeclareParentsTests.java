package org.springframework.aop.aspectj;

import org.springframework.aop.framework.Lockable;
import org.springframework.aop.support.AopUtils;

/**
 * 
 * 
 * @author Rod Johnson
 *
 */
public class DeclareParentsTests extends AbstractAdviceBindingTests {

	protected String[] getConfigLocations() {
		return new String[] {
				"org/springframework/aop/aspectj/declare-parents-tests.xml"};
	}
	
	public void testIntroductionWasMade() {
		assertTrue("Introduction must have been made", testBeanProxy instanceof Lockable);
	}
	
	// TODO if you change type pattern from org.springframework.beans..*
	// to org.springframework..* it also matches introduction.
	// Perhaps generated advisor bean definition could be made to depend
	// on the introduction, in which case this would not be a problem
	
	public void testLockingWorks() {
		Object introductionObject = applicationContext.getBean("introduction");
		assertFalse("Introduction should not be proxied", AopUtils.isAopProxy(introductionObject));
		
		Lockable lockable = (Lockable) testBeanProxy;
		assertFalse(lockable.locked());
		
		// Invoke a non-advised method
		testBeanProxy.getAge();
		
		testBeanProxy.setName("");
		lockable.lock();
		try {
			testBeanProxy.setName(" ");
			fail("Should be locked");
		}
		catch (IllegalStateException ex) {
			
		}
	}

}
