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

package org.springframework.aop.aspectj.autoproxy.benchmark;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;

/**
 * Tests for AspectJ auto proxying. Includes mixing with Spring AOP 
 * Advisors to demonstrate that existing autoproxying contract is honoured.
 * 
 * @author Rod Johnson
 *
 */
public class BenchmarkTests extends TestCase {
	
	protected int getCount() {
		return 1000000;
	}
	
	public void testRepeatedInvocationsWithAspectJ() {
		testRepeatedInvocations("/org/springframework/aop/aspectj/autoproxy/aspects.xml", getCount(), "AspectJ");
	}
	
	public void testRepeatedInvocationsWithSpringAop() {
		testRepeatedInvocations("/org/springframework/aop/aspectj/autoproxy/benchmark/springAop.xml", getCount(), "Spring AOP");
	}
	
	private long testRepeatedInvocations(String file, int howmany, String technology) {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext(file);

		StopWatch sw = new StopWatch();
		sw.start(howmany + " repeated invocations with " + technology);
		ITestBean adrian = (ITestBean) bf.getBean("adrian");
		
		assertTrue(AopUtils.isAopProxy(adrian));
		assertEquals(68, adrian.getAge());
		
		for (int i = 0; i < howmany; i++) {
			adrian.getAge();
		}
		
		sw.stop();
		System.out.println(sw.prettyPrint());
		return sw.getLastTaskTimeMillis();
	}
	
	public static void main(String[] args) {
		new BenchmarkTests().testRepeatedInvocationsWithAspectJ();
	}
	
}
