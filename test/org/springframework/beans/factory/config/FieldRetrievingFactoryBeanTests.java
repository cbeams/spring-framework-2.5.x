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

package org.springframework.beans.factory.config;

import java.sql.Connection;

import junit.framework.TestCase;

/**
 * @author Juergen Hoeller
 * @since 31.07.2004
 */
public class FieldRetrievingFactoryBeanTests extends TestCase {

	public void testStaticField() throws Exception {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		fr.setStaticField("java.sql.Connection.TRANSACTION_SERIALIZABLE");
		fr.afterPropertiesSet();
		assertEquals(new Integer(Connection.TRANSACTION_SERIALIZABLE), fr.getObject());
	}

	public void testStaticFieldViaClassAndFieldName() throws Exception {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		fr.setTargetClass(Connection.class);
		fr.setTargetField("TRANSACTION_SERIALIZABLE");
		fr.afterPropertiesSet();
		assertEquals(new Integer(Connection.TRANSACTION_SERIALIZABLE), fr.getObject());
	}

	public void testNonStaticField() throws Exception {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		PublicFieldHolder target = new PublicFieldHolder();
		fr.setTargetObject(target);
		fr.setTargetField("publicField");
		fr.afterPropertiesSet();
		assertEquals(target.publicField, fr.getObject());
	}

	public void testNotConfigured() throws NoSuchFieldException {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		try {
			fr.afterPropertiesSet();
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testJustTargetField() throws NoSuchFieldException {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		fr.setTargetField("TRANSACTION_SERIALIZABLE");
		try {
			fr.afterPropertiesSet();
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testJustTargetClass() throws NoSuchFieldException {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		fr.setTargetClass(Connection.class);
		try {
			fr.afterPropertiesSet();
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testJustTargetObject() throws NoSuchFieldException {
		FieldRetrievingFactoryBean fr = new FieldRetrievingFactoryBean();
		fr.setTargetObject(new PublicFieldHolder());
		try {
			fr.afterPropertiesSet();
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}


	private static class PublicFieldHolder {

		public String publicField = "test";
	}

}
