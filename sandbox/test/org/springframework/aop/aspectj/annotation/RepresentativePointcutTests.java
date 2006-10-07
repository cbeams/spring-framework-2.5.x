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
package org.springframework.aop.aspectj.annotation;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Rod Johnson
 * @since 2.0
 */
public class RepresentativePointcutTests extends AbstractAnnotationPointcutTests {
	
	// Will be replaced by a proxy
	private TestAspect testAspect;

	public void testPointcutWithoutArguments() {
		testAspect.returnsVoid();
		
		ITestBean itb = (ITestBean) getMatchingProxy(new TestBean());//, "execution(* *et*(..))");
		itb.setAge(0);
		//itb.getAge();
	}
	
	public void testNoArgumentsBeforeAdvice() throws Throwable {
		// Just to identify it. Survives refactoring.
		testAspect.etMethods();
		
		ITestBean itb = (ITestBean) getMatchingProxy(new TestBean());//, "execution(* *et*(..))");
		itb.getAge();
		itb.setName("");
		//itb.exceptional(null);
	}
	
	public void testAfterReturningAdviceWithReturnValue() throws Throwable {
		// Just to identify it. Survives refactoring.
		testAspect.intGetters(0);
		
		ITestBean itb = (ITestBean) getMatchingProxy(new TestBean());//, "execution(* *et*(..))");
		assertNotNull(itb);
		itb.getAge();
		//itb.setName("");
		//itb.exceptional(null);
	}
	
	public void testBeforeAdviceWithStringArgument() throws Throwable {
		// Just to identify it. Survives refactoring.
		testAspect.settersTakingString(null);
		
		ITestBean itb = (ITestBean) getMatchingProxy(new TestBean());//, "execution(* *et*(..))");
		assertNotNull(itb);
		//itb.getAge();
		itb.setName("");
		//itb.exceptional(null);
	}
	
	// TODO assertions: allMethods(class), noMethods(class)
	
//	public void testSomethingDoesNotMatch() throws Throwable {		
//		ITestBean itb = (ITestBean) getNonMatchingProxy(new TestBean(), "execution(* *et*(..))");
//		assertNotNull(itb);
//		//itb.getAge();
//		//itb.setName("");
//		itb.exceptional(null);
//	}
//	
//	
////	@PointcutTest(clazz=TestBean.class, 
////			method="foo")
//	public void testSomethingDoesMatch() throws Throwable {		
//		// TODO needs to know param names and types.
//		// Get from annotation and perhaps other strategy also
//		
//		ITestBean itb = (ITestBean) getMatchingProxy(new TestBean(), 
//				"execution(* set*(String)) && args(s)");
//		assertNotNull(itb);
//		//itb.getAge();
//		itb.setName("");
//		//itb.exceptional(null);
//	}
	

}
