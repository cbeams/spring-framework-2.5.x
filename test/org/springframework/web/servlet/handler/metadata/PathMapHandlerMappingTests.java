/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.handler.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;

/**
 * @author Rod Johnson
 * @version $Id: PathMapHandlerMappingTests.java,v 1.2 2003-12-30 01:16:35 jhoeller Exp $
 */
public class PathMapHandlerMappingTests extends TestCase {
	
	public void testSatisfiedConstructorDependency() throws Exception {
		String path = "/Constructor.htm";
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.registerSingleton("test", TestBean.class, new MutablePropertyValues());
		int oldCount = wac.getBeanDefinitionCount();

		HashUrlMapHandlerMapping hm = new HashUrlMapHandlerMapping();
		hm.register(ConstructorController.class, new PathMap(path));
		hm.setApplicationContext(wac);
		ConstructorController cc = (ConstructorController) wac.getBean(ConstructorController.class.getName());
		assertSame(wac.getBean("test"), cc.testBean);
		HandlerExecutionChain chain = hm.getHandler(new MockHttpServletRequest(null, "GET", path));
		assertEquals("Path is mapped correctly based on attribute", cc, chain.getHandler());
		chain = hm.getHandler(new MockHttpServletRequest(null, "GET", "completeRubbish.html"));
		assertNull("Don't know anything about this path", chain);
	}

	public void testUnsatisfiedConstructorDependency() throws Exception {
		String path = "/Constructor.htm";
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		// No registration of a TestBean
		//wac.registerSingleton("test", TestBean.class, new MutablePropertyValues());

		HashUrlMapHandlerMapping hm = new HashUrlMapHandlerMapping();
		hm.register(ConstructorController.class, new PathMap(path));
		try {
			hm.setApplicationContext(wac);
			fail("DependencyCheck should have failed");
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
		}
	}

	public void testMultiplePaths() throws Exception {
		String path1 = "/Constructor.htm";
		String path2 = "path2.cgi";
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.registerSingleton("test", TestBean.class, new MutablePropertyValues());
		int oldCount = wac.getBeanDefinitionCount();

		HashUrlMapHandlerMapping hm = new HashUrlMapHandlerMapping();
		hm.register(ConstructorController.class, new PathMap[] { new PathMap(path1), new PathMap(path2) });
		hm.setApplicationContext(wac);
		ConstructorController cc = (ConstructorController) wac.getBean(ConstructorController.class.getName());
		assertSame(wac.getBean("test"), cc.testBean);
		HandlerExecutionChain chain = hm.getHandler(new MockHttpServletRequest(null, "GET", path1));
		assertEquals("Path is mapped correctly based on attribute 1", cc, chain.getHandler());
		chain = hm.getHandler(new MockHttpServletRequest(null, "GET", "/" + path2));
		assertEquals("Path is mapped correctly based on attribute 2", cc, chain.getHandler());
		chain = hm.getHandler(new MockHttpServletRequest(null, "GET", "completeRubbish.html"));
		assertNull("Don't know anything about this path", chain);
	}

	
	private static class HashUrlMapHandlerMapping extends AbstractPathMapHandlerMapping {

		private HashMap classToPathMaps = new HashMap();

		public void register(Class clazz, PathMap pm) {
			classToPathMaps.put(clazz, new PathMap[] { pm });
		}

		public void register(Class clazz, PathMap[] pms) {
			classToPathMaps.put(clazz, pms);
		}

		protected Collection getClassNamesWithPathMapAttributes() {
			Collection names = new ArrayList(classToPathMaps.size());
			for (Iterator itr = classToPathMaps.keySet().iterator(); itr.hasNext(); ) {
				Class clazz = (Class) itr.next();
				names.add(clazz.getName());
			}
			return names;
		}

		protected PathMap[] getPathMapAttributes(Class handlerClass) {
			return (PathMap[]) classToPathMaps.get(handlerClass);
		}
	}

}
