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
package org.springframework.jms.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 * @author Juergen Hoeller
 * @since 26.07.2004
 */
public class SingleConnectionFactoryTests extends TestCase {

	public void testWithConnection() throws JMSException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();

		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory(con);
		Connection con1 = scf.createConnection();
		con1.start();
		con1.close();  // should be ignored
		Connection con2 = scf.createConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		conControl.verify();
	}

	public void testWithQueueConnection() throws JMSException {
		MockControl conControl = MockControl.createControl(QueueConnection.class);
		Connection con = (QueueConnection) conControl.getMock();

		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory(con);
		QueueConnection con1 = scf.createQueueConnection();
		con1.start();
		con1.close();  // should be ignored
		QueueConnection con2 = scf.createQueueConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		conControl.verify();
	}

	public void testWithTopicConnection() throws JMSException {
		MockControl conControl = MockControl.createControl(TopicConnection.class);
		Connection con = (TopicConnection) conControl.getMock();

		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory(con);
		TopicConnection con1 = scf.createTopicConnection();
		con1.start();
		con1.close();  // should be ignored
		TopicConnection con2 = scf.createTopicConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		conControl.verify();
	}

	public void testWithConnectionFactory() throws JMSException {
		MockControl cfControl = MockControl.createControl(ConnectionFactory.class);
		ConnectionFactory cf = (ConnectionFactory) cfControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();

		cf.createConnection();
		cfControl.setReturnValue(con, 1);
		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		cfControl.replay();
		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory(cf);
		Connection con1 = scf.createConnection();
		con1.start();
		con1.close();  // should be ignored
		Connection con2 = scf.createConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		cfControl.verify();
		conControl.verify();
	}

	public void testConnectionFactory102WithQueue() throws JMSException {
		MockControl cfControl = MockControl.createControl(QueueConnectionFactory.class);
		QueueConnectionFactory cf = (QueueConnectionFactory) cfControl.getMock();
		MockControl conControl = MockControl.createControl(QueueConnection.class);
		QueueConnection con = (QueueConnection) conControl.getMock();

		cf.createQueueConnection();
		cfControl.setReturnValue(con, 1);
		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		cfControl.replay();
		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory102(cf, false);
		QueueConnection con1 = scf.createQueueConnection();
		con1.start();
		con1.close();  // should be ignored
		QueueConnection con2 = scf.createQueueConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		cfControl.verify();
		conControl.verify();
	}

	public void testConnectionFactory102WithTopic() throws JMSException {
		MockControl cfControl = MockControl.createControl(TopicConnectionFactory.class);
		TopicConnectionFactory cf = (TopicConnectionFactory) cfControl.getMock();
		MockControl conControl = MockControl.createControl(TopicConnection.class);
		TopicConnection con = (TopicConnection) conControl.getMock();

		cf.createTopicConnection();
		cfControl.setReturnValue(con, 1);
		con.start();
		conControl.setVoidCallable(2);
		con.close();
		conControl.setVoidCallable(1);

		cfControl.replay();
		conControl.replay();

		SingleConnectionFactory scf = new SingleConnectionFactory102(cf, true);
		TopicConnection con1 = scf.createTopicConnection();
		con1.start();
		con1.close();  // should be ignored
		TopicConnection con2 = scf.createTopicConnection();
		con2.start();
		con2.close();  // should be ignored
		scf.destroy();  // should trigger actual close

		cfControl.verify();
		conControl.verify();
	}

}
