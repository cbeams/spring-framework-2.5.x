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

package org.springframework.beans.factory.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.core.io.Resource;
import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 */
public class ResourceFactoryBeanTests extends TestCase {

	public void testIsSingleton() throws Exception {
		ResourceFactoryBean factory = new ResourceFactoryBean();
		assertTrue(factory.isSingleton());
	}

	public void testGetObjectTypeDefaultsToPlainResourceInterfaceifLookupResourceIsNotSupplied() throws Exception {
		ResourceFactoryBean factory = new ResourceFactoryBean();
		assertEquals(Resource.class, factory.getObjectType());
	}

	public void testGetObjectTypeReturnsActualResourceLocationTypeWhenLookupResourceIsSupplied() throws Exception {
		ResourceFactoryBean factory = new ResourceFactoryBean();
		factory.setLocation(new StubResource());
		assertEquals(StubResource.class, factory.getObjectType());
	}

	public void testReturnsResourceAsIsInSunnyDayCase() throws Exception {
		MockControl mockResource = MockControl.createControl(Resource.class);
		Resource resource = (Resource) mockResource.getMock();
		mockResource.replay();

		ResourceFactoryBean factory = new ResourceFactoryBean();
		factory.setLocation(resource);
		factory.afterPropertiesSet();
		Object object = factory.getObject();

		assertNotNull("As per FactoryBean contract, the return value of getObject() cannot be null.", object);
		assertTrue("Obviously not getting a Resource back", Resource.class.isAssignableFrom(object.getClass()));
		assertSame("Not passing Resource location back as-is", resource, object);

		mockResource.verify();
	}

	public void testWhenResourceLocationIsMissing() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				ResourceFactoryBean factory = new ResourceFactoryBean();
				factory.afterPropertiesSet();
			}
		}.runTest();
	}


	private static class StubResource implements Resource {

		public boolean exists() {
			throw new UnsupportedOperationException();
		}

		public boolean isOpen() {
			throw new UnsupportedOperationException();
		}

		public URL getURL() {
			throw new UnsupportedOperationException();
		}

		public File getFile() {
			throw new UnsupportedOperationException();
		}

		public Resource createRelative(String relativePath) {
			throw new UnsupportedOperationException();
		}

		public String getFilename() {
			throw new UnsupportedOperationException();
		}

		public String getDescription() {
			throw new UnsupportedOperationException();
		}

		public InputStream getInputStream() {
			throw new UnsupportedOperationException();
		}

	}

}
