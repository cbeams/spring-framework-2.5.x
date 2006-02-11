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

package org.springframework.jmx.export;

import junit.framework.TestCase;

/**
 * Unit tests for the NotificationListenerBean class.
 *
 * @author Rick Evans
 */
public final class NotificationListenerBeanTests extends TestCase {

	public void testSetMappedObjectNameForNullMappedObjectNameTarget() throws Exception {
		try {
			new NotificationListenerBean().setMappedObjectName(null);
			fail("Must have thrown an IllegalArgumentException by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testCreationWithNoNotificationListenerSet() throws Exception {
		try {
			new NotificationListenerBean().afterPropertiesSet();
			fail("Must have thrown an IllegalArgumentException (no NotificationListener supplied).");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testSetMappedObjectNamesWithNull() throws Exception {
		try {
			new NotificationListenerBean().setMappedObjectNames((String[]) null);
			fail("Must have thrown an IllegalArgumentException by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testSetMappedObjectNamesWithEmptyStringArray() throws Exception {
		try {
			new NotificationListenerBean().setMappedObjectNames(new String[] {});
			fail("Must have thrown an IllegalArgumentException by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testSetMappedObjectNamesWithSparseNullStringArray() throws Exception {
		try {
			new NotificationListenerBean().setMappedObjectNames(new String[] {null});
			fail("Must have thrown an IllegalArgumentException by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

}
