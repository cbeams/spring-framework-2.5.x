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

package org.springframework.remoting.caucho;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.server.BurlapSkeleton;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.util.NestedServletException;

/**
 * HTTP request handler that exports the specified service bean as
 * Burlap service endpoint, accessible via a Burlap proxy.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>This exporter will work with both Burlap 2.x and 3.x (respectively
 * Resin 2.x and 3.x), autodetecting the corresponding skeleton class.
 *
 * <p>Note: Burlap services exported with this class can be accessed by
 * any Burlap client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see BurlapClientInterceptor
 * @see BurlapProxyFactoryBean
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 * @see org.springframework.remoting.rmi.RmiServiceExporter
 */
public class BurlapServiceExporter extends RemoteExporter
		implements HttpRequestHandler, InitializingBean {

	private BurlapSkeleton skeleton;


	public void afterPropertiesSet() {
		prepare();
	}

	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		try {
			try {
				// Try Burlap 3.x (with service interface argument).
				Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
				checkService();
				checkServiceInterface();
				this.skeleton = (BurlapSkeleton)
						ctor.newInstance(new Object[] {getProxyForService(), getServiceInterface()});
			}
			catch (NoSuchMethodException ex) {
				// Fall back to Burlap 2.x (without service interface argument).
				Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class});
				this.skeleton = (BurlapSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
			}
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Burlap skeleton initialization failed", ex);
		}
	}


	/**
	 * Processes the incoming Burlap request and creates a Burlap response.
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Assert.notNull(this.skeleton, "BurlapServiceExporter has not been initialized");

		if (!"POST".equals(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException("POST",
					"BurlapServiceExporter only supports POST requests");
		}

		BurlapInput in = new BurlapInput(request.getInputStream());
		BurlapOutput out = new BurlapOutput(response.getOutputStream());
		try {
		  this.skeleton.invoke(in, out);
		}
		catch (Throwable ex) {
		  throw new NestedServletException("Burlap skeleton invocation failed", ex);
		}
	}

}
