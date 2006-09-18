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
package org.springframework.instrument.classloading.tomcat;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.naming.resources.FileDirContext;
import org.easymock.MockControl;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader;
import org.springframework.util.ReflectionUtils;

public class TomcatInstrumentedClassLoaderTests extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testClassloader() throws Exception {
		ClassLoader parent = new URLClassLoader(new URL[] {});
		ExtendedTomcatClassLoader classloader = new ExtendedTomcatClassLoader(parent);
		ResourceLoader loader = new DefaultResourceLoader();
		//classloader.addRepository("file:" + new File("bin").getAbsolutePath());
		classloader.addRepo(new File("bin").getAbsolutePath(), null);
		classloader.setResources(new FileDirContext());
		classloader.start();
		MockControl ctrl = MockControl.createStrictControl(ClassFileTransformer.class);
		ClassFileTransformer transformer = (ClassFileTransformer) ctrl.getMock();

		getClass().getClassLoader().loadClass(getClass().getName());
		classloader.loadClass(getClass().getName());
		ctrl.replay();
		classloader.addTransformer(transformer);
		try {
			classloader.findClass("java.lang.Object");
			fail("should not delegate to parent");
		}
		catch (ClassNotFoundException e) {
			// it's okay
		}
		classloader.findClass(this.getClass().getName());

		ctrl.verify();
	}

	private static class ExtendedTomcatClassLoader extends TomcatInstrumentableClassLoader {
		public ExtendedTomcatClassLoader() {
			super();
		}

		public ExtendedTomcatClassLoader(ClassLoader cl) {
			super(cl);
		}

		public void addRepo(String repository, File file) throws Exception {
			Method method = WebappClassLoader.class.getDeclaredMethod("addRepository", new Class[] { String.class, File.class });
			method.setAccessible(true);
			ReflectionUtils.invokeMethod(method, this, new Object[] { repository, file });
		}
	}
}
