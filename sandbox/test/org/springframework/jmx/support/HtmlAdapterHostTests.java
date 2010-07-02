/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.support;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.management.MalformedObjectNameException;

import org.springframework.jmx.AbstractJmxTests;

/**
 * @author Rob Harrop
 */
public class HtmlAdapterHostTests extends AbstractJmxTests {

	public void testStartAndStop() throws Exception {
		HtmlAdapterHost host = getAdapter();
		host.afterPropertiesSet();

		// attempt to connect
		URL url = new URL("http://localhost:9090");
		assertAdapter(url);

		host.stop();
	}

	public void testStartAndStopWithConfiguredPort() throws Exception {
		HtmlAdapterHost host = getAdapter();
		host.setPort(9001);
		host.afterPropertiesSet();

		// attempt to connect
		URL url = new URL("http://localhost:9001");
		assertAdapter(url);

		host.stop();
	}

	public void testStartAndStopWithLocatedServer() throws Exception {
		HtmlAdapterHost host = getAdapter();
		host.setServer(null);
		host.afterPropertiesSet();

		// attempt to connect
		URL url = new URL("http://localhost:9090");
		assertAdapter(url);

		host.stop();
	}

	private void assertAdapter(URL url) throws IOException {
		URLConnection connection = url.openConnection();

		// validate connection
		assertNotNull(connection);
		assertEquals("text/html", connection.getContentType());
		assertTrue(connection.getContentLength() > 0);
	}

	private HtmlAdapterHost getAdapter() throws MalformedObjectNameException {
		HtmlAdapterHost host = new HtmlAdapterHost();
		host.setObjectName("adapter:name=test");
		host.setServer(this.server);
		return host;
	}

}
