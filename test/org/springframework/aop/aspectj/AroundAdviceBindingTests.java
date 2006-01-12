/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.aop.aspectj;

import org.springframework.aop.aspectj.AdviceBindingTestAspect.AdviceBindingCollaborator;
import org.springframework.aop.aspectj.AroundAdviceBindingTestAspect.AroundAdviceBindingCollaborator;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.TestBean;
import org.springframework.beans.ITestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.easymock.MockControl;

/**
 * Tests for various parameter binding scenarios with before advice
 * @author Adrian Colyer
 */
public class AroundAdviceBindingTests extends
		AbstractDependencyInjectionSpringContextTests {

	private AroundAdviceBindingTestAspect aroundAdviceAspect;
	private ITestBean testBean;
	private TestBean testBeanTarget;
	private MockControl mockControl;
	private AroundAdviceBindingCollaborator mockCollaborator;
	
	public void setAroundAdviceAspect(AroundAdviceBindingTestAspect anAspect) {
		this.aroundAdviceAspect = anAspect;
	}
	
	public void setTestBean(ITestBean aBean) throws Exception {
		this.testBean = aBean;
		// we need the real target too, not just the proxy...
		this.testBeanTarget = (TestBean) ((Advised)aBean).getTargetSource().getTarget();
	}

	// simple test to ensure all is well with the xml file
	// note that this implicitly tests that the arg-names binding is working
	public void testParse() {}
	
	public void testOneIntArg() {
		mockCollaborator.oneIntArg(5);
		mockControl.replay();
		testBean.setAge(5);
		mockControl.verify();
	}
	
	public void testOneObjectArg() {
		mockCollaborator.oneObjectArg(this.testBeanTarget);
		mockControl.replay();
		testBean.getAge();
		mockControl.verify();
	}
	
	public void testOneIntAndOneObjectArgs() {
		mockCollaborator.oneIntAndOneObject(5,this.testBeanTarget);
		mockControl.replay();
		testBean.setAge(5);
		mockControl.verify();
	}
	
	public void testJustJoinPoint() {
		mockCollaborator.justJoinPoint("getAge");
		mockControl.replay();
		testBean.getAge();
		mockControl.verify();
	}
	
	protected String[] getConfigLocations() {
		return new String[] {"org/springframework/aop/aspectj/around-advice-tests.xml"};
	}
	
	protected void onSetUp() throws Exception {
		super.onSetUp();
		mockControl = MockControl.createNiceControl(AroundAdviceBindingCollaborator.class);
		mockCollaborator = (AroundAdviceBindingCollaborator) mockControl.getMock();
		aroundAdviceAspect.setCollaborator(mockCollaborator);
	}

	
}
