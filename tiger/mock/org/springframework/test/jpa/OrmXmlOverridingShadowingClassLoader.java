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

package org.springframework.test.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.springframework.instrument.classloading.ShadowingClassLoader;
import org.springframework.instrument.classloading.ShadowingClassLoader;

/**
 * Subclass of ShadowingClassLoader that overrides attempts to
 * locate <code>orm.xml</code>.
 *
 * <p>This class must <b>not</b> be an inner class of AbstractJpaTests
 * to avoid it being loaded until first used.
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
class OrmXmlOverridingShadowingClassLoader extends ShadowingClassLoader {
	
	private List<String> providerPrefixes = new LinkedList<String>(); {
		// Automatically exclude classes from these well-known persistence providers
		providerPrefixes.add("oracle.toplink.essentials");
		providerPrefixes.add("kodo");
		
		// Do NOT exclude Hibernate classes --
		// this causes class casts due to use of
		// CGLIB by Hibernate
	}
	
	private static final Enumeration<URL> EMPTY_URL_ENUMERATION = new Enumeration<URL>() {

		public boolean hasMoreElements() {
			return false;
		}

		public URL nextElement() {
			throw new UnsupportedOperationException("Should not be called. I am empty");
		}
	};
	
	private final String realOrmXmlLocation;

	OrmXmlOverridingShadowingClassLoader(ClassLoader loader, String realOrmXmlLocation) {
		super(loader);
		this.realOrmXmlLocation = realOrmXmlLocation;
	}
	
	@Override
	protected boolean isClassNameExcludedFromShadowing(String className) {
		for (String providerPrefix : providerPrefixes) {
			if (className.startsWith(providerPrefix)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean askingForDefaultOrmXmlLocation(String requestedPath) {
		return "/META-INF/orm.xml".equals(requestedPath) ||
					"META-INF/orm.xml".equals(requestedPath);
	}

	@Override
	public URL getResource(String requestedPath) {
		if (askingForDefaultOrmXmlLocation(requestedPath)) {
			return realOrmXmlLocation == null ? 
					null : 
					super.getResource(this.realOrmXmlLocation);
		}
		else {
			return super.getResource(requestedPath);
		}
	}

	@Override
	public InputStream getResourceAsStream(String requestedPath) {
		if (askingForDefaultOrmXmlLocation(requestedPath)) {
			return realOrmXmlLocation == null ? null : super.getResourceAsStream(this.realOrmXmlLocation);
		}
		else {
			return super.getResourceAsStream(requestedPath);
		}
	}
	
	@Override
	public Enumeration<URL> getResources(String requestedPath) throws IOException {
		if (askingForDefaultOrmXmlLocation(requestedPath)) {
			return realOrmXmlLocation == null ? 
					EMPTY_URL_ENUMERATION :
					super.getResources(this.realOrmXmlLocation);
		}
		else {
			return super.getResources(requestedPath);
		}
	}

}
