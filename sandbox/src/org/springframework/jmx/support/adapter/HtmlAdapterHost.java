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

package org.springframework.jmx.support.adapter;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * @author Rob Harrop
 */
public class HtmlAdapterHost extends AbstractAdapterHost {

	private int port = 9090;

	private HtmlAdaptorServer adapter;

	public void setPort(int port) {
		this.port = port;
	}

	protected void initAdapterHost() {
		this.adapter = new HtmlAdaptorServer(this.port);
	}

	protected Object getAdapterMBean() {
		return this.adapter;
	}

	public void start() {
		this.adapter.start();
	}

	public void stop() {
		this.adapter.stop();
	}

}
