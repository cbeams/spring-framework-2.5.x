/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.aop.framework;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;

/**
 * 
 * @author Rod Johnson
 */
public class AopProxyUtilsTests extends TestCase {
	
	public void testCompleteProxiedInterfacesWorksWithNull() {
		AdvisedSupport as = new AdvisedSupport();
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(1, completedInterfaces.length);
		assertEquals(Advised.class, completedInterfaces[0]);
	}
	
	public void testCompleteProxiedInterfacesWorksWithNullOpaque() {
		AdvisedSupport as = new AdvisedSupport();
		as.setOpaque(true);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(0, completedInterfaces.length);
	}
	
	public void testCompleteProxiedInterfacesAdvisedNotIncluded() {
		AdvisedSupport as = new AdvisedSupport();
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(3, completedInterfaces.length);
		
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertTrue(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}
	
	public void testCompleteProxiedInterfacesAdvisedIncluded() {
		AdvisedSupport as = new AdvisedSupport();
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		as.addInterface(Advised.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(3, completedInterfaces.length);
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertTrue(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}
	
	public void testCompleteProxiedInterfacesAdvisedNotIncludedOpaque() {
		AdvisedSupport as = new AdvisedSupport();
		as.setOpaque(true);
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(2, completedInterfaces.length);
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertFalse(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}

}