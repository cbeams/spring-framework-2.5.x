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

package org.springframework.jmx.support;

//import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * Implementation of <code>AdapterHost</code> for the <code>HtmlAdaptorServer</code>
 * included in the JMX Reference Implementation.
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class HtmlAdapterHost extends AbstractAdapterHost {

	/**
	 * The default port for the adapter to listen on.
	 */
	private int port = 9090;

	/**
	 * The <code>HtmlAdaptorServer</code> instance.
	 */
	private Object adapter;


	/**
	 * Set the port that the adapter should listen on.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Creates the adapter instance.
	 */
	protected void initAdapterHost() {
		//this.adapter = new HtmlAdaptorServer(this.port);
	}

	/**
	 * Exposes the HtmlAdaptorServer instance.
	 */
	protected Object getAdapterMBean() {
		return this.adapter;
	}

	/**
	 * Start the HtmlAdaptorServer.
	 */
	public void start() {
		//this.adapter.start();
	}

	/**
	 * Stop the HtmlAdaptorServer.
	 */
	public void stop() {
		//this.adapter.stop();
	}

}
