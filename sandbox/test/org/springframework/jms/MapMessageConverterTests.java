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

package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 * Test the converter for map message and objects.
 * @author Mark Pollack
 */
public class MapMessageConverterTests extends TestCase {

	private MockControl _sessionControl;

	private Session _mockSession;

	protected void setUp() throws Exception {
		_sessionControl = MockControl.createControl(Session.class);
		_mockSession = (Session) _sessionControl.getMock();
	}

	public void testSimplePropertiesMarshall() throws Exception {
		MapMessageConverter c = new MapMessageConverter("com.foo.");
		assertEquals("Wrong package name", "com.foo", c.getPackageName());
		c.setUnqualifiedClassnameFieldName("UNQUAL__");
		assertEquals("Unqualified fieldname property not working", "UNQUAL__", c.getUnqualifiedClassnameFieldName());
	}

	private void testSimpleBean(MapMessageConverter c) throws JMSException {
		MockControl messageControl =
		    MockControl.createControl(MapMessage.class);
		MapMessage mockMessage = (MapMessage) messageControl.getMock();

		SimpleTestBean tb = new SimpleTestBean();
		tb.setName("Mark");
		tb.setAge(35.5);

		//Session behavior
		_mockSession.createMapMessage();
		_sessionControl.setReturnValue(mockMessage);

		//Message behavior
		mockMessage.setString("unq__", "SimpleTestBean");
		mockMessage.setObject("age", new Double(35.5));
		mockMessage.setObject("name", "Mark");

		_sessionControl.replay();
		messageControl.replay();

		Message m = c.toMessage(tb, _mockSession);

		_sessionControl.verify();
		messageControl.verify();
	}

}
