/*
 * Copyright 2002-2007 the original author or authors.
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

import junit.framework.TestCase;
import org.springframework.test.AssertThrows;
import org.springframework.beans.TestBean;
import org.springframework.beans.ITestBean;
import org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer;

import java.lang.reflect.Method;

/**
 * @author Adrian Colyer
 * @since 2.0.3
 */
public class ArgumentBindingTests extends TestCase {

    public void testBindingInPointcutUsedByAdvice() {
        TestBean tb = new TestBean();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(tb);
        proxyFactory.addAspect(NamedPointcutWithArgs.class);
        final ITestBean proxiedTestBean = (ITestBean) proxyFactory.getProxy();
        new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
                proxiedTestBean.setName("Supercalifragalisticexpialidocious");
            }
		}.runTest();
	}

    public void testParameterNameDiscoverWithReferencePcut() throws NoSuchMethodException {
        AspectJAdviceParameterNameDiscoverer discoverer =
                new AspectJAdviceParameterNameDiscoverer("somepc(formal) && set(* *)");
        discoverer.setRaiseExceptions(true);
        Method methodUsedForParameterTypeDiscovery =
                getClass().getMethod("methodWithOneParam",String.class);
        String[] pnames = discoverer.getParameterNames(methodUsedForParameterTypeDiscovery);
        assertEquals("one parameter name",1,pnames.length);
        assertEquals("formal",pnames[0]);
    }

    public void methodWithOneParam(String aParam) {}
}
