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

package org.springframework.beans.factory;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * 
 * @author Rod Johnson
 */
public class ConfigurerTests extends TestCase {
    
    public void testOnBeanFactoryNoProcessors() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        Configurer cfg = new Configurer(bf);
        cfg.add("testBean", TestBean.class).
        	prop("name", "tom");
        //cfg.apply();
        
        System.out.println(bf);
        ITestBean tb = (ITestBean) bf.getBean("testBean");
        assertEquals("tom", tb.getName());
    }
    
    public void testOnBeanFactoryNoProcessorsWithRecording() {
        String beckyName = "becky";
        
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        Configurer cfg = new Configurer(bf);
        //cfg.setDefaultAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);
        
        // TODO possibility of infinite loop...
        TestBean becky = (TestBean) cfg.add("becky", TestBean.class).
        	singleton(false).
        	prop("name", beckyName).
        	ref("spouse", "tom");
        	
        System.err.println(((Advised) becky).toProxyConfigString());
        
       // TestBean becky = (TestBean) cfg.recordable();
        //becky.setName(beckyName);
        
        TestBean tom = (TestBean) cfg.add("tom", TestBean.class);
        tom.setName("tom");
        tom.setSpouse(becky);
        tom.setAge(24);
        
        //cfg.apply();
        
        System.out.println(bf);
        ITestBean tb = (ITestBean) bf.getBean("tom");
        assertEquals(24, tb.getAge());
        assertEquals("tom", tb.getName());
        assertEquals(beckyName, tb.getSpouse().getName());
        
        ITestBean becky1 = (ITestBean) bf.getBean("becky");
        ITestBean becky2 = (ITestBean) bf.getBean("becky");
        assertNotSame(becky1, becky2);
        assertEquals(beckyName, becky1.getName());
        assertEquals(beckyName, becky2.getName());
        assertSame(tb, becky1.getSpouse());
        assertSame(tb, becky2.getSpouse());
    }

//    public void testOnApplicationContextWithPostProcessors() {
//        AbstractApplicationContext ac = new ParameterizableApplicationContext();
//        NopInterceptor nop = new NopInterceptor();
//        ac.getBeanFactory().registerSingleton("nopInterceptor", nop);
//        BeanNameAutoProxyCreator bnapc = new BeanNameAutoProxyCreator();
//        bnapc.setInterceptorNames(new String[] { "nopInterceptor"});
//        bnapc.setBeanNames(new String[] { "test*" });
//        bnapc.setBeanFactory(ac.getBeanFactory());
//        
//        ac.getBeanFactory().addBeanPostProcessor(bnapc);
//        JavaBeanDefinitionReader jbr = new JavaBeanDefinitionReader((BeanDefinitionRegistry) ac.getBeanFactory());
//        assertEquals(1, jbr.addDefinitions(MyBeans.class));
//        System.out.println(ac);
//        ITestBean tb = (ITestBean) ac.getBean("testBean");
//        assertEquals("tom", tb.getName());
//        
//        assertTrue(AopUtils.isAopProxy(tb));
//        assertEquals(1, nop.getCount());
//    }

}
