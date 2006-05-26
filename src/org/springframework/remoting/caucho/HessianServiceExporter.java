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

package org.springframework.remoting.caucho;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.server.HessianSkeleton;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.util.NestedServletException;

/**
 * Web controller that exports the specified service bean as Hessian service
 * endpoint, accessible via a Hessian proxy.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>This exporter will work with both Hessian 2.x and 3.x (respectively
 * Resin 2.x and 3.x), auto-detecting the corresponding skeleton class.
 *
 * <p>Note: Hessian services exported with this class can be accessed by
 * any Hessian client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see HessianClientInterceptor
 * @see HessianProxyFactoryBean
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 * @see org.springframework.remoting.rmi.RmiServiceExporter
 */
public class HessianServiceExporter extends RemoteExporter
		implements HttpRequestHandler, InitializingBean {

	private HessianSkeleton skeleton;


	public void afterPropertiesSet() {
		prepare();
	}

	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		try {
			try {
				// Try Hessian 3.x (with service interface argument).
				Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
				checkService();
				checkServiceInterface();
				this.skeleton = (HessianSkeleton)
						ctor.newInstance(new Object[] {getProxyForService(), getServiceInterface()});
			}
			catch (NoSuchMethodException ex) {
				// Fall back to Hessian 2.x (without service interface argument).
				Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class});
				this.skeleton = (HessianSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
			}
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Hessian skeleton initialization failed", ex);
		}
	}


	/**
	 * Process the incoming Hessian request and create a Hessian response.
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Assert.notNull(this.skeleton, "HessianServiceExporter has not been initialized");

		if (!"POST".equals(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(
					"POST", "HessianServiceExporter only supports POST requests");
		}

		HessianInput in = new HessianInput(request.getInputStream());
		HessianOutput out = new HessianOutput(response.getOutputStream());
		try {
		  this.skeleton.invoke(in, out);
		}
		catch (Throwable ex) {
		  throw new NestedServletException("Hessian skeleton invocation failed", ex);
		}
	}

}
