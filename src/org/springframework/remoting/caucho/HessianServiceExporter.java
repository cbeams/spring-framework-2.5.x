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

package org.springframework.remoting.caucho;

import java.lang.reflect.Constructor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.server.HessianSkeleton;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

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
 * @see HessianProxyFactoryBean
 */
public class HessianServiceExporter extends RemoteExporter implements Controller, InitializingBean {

	private HessianSkeleton skeleton;

	public void afterPropertiesSet() throws Exception {
		try {
			// try Hessian 3.x (with service interface argument)
			Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
			this.skeleton = (HessianSkeleton) ctor.newInstance(new Object[] {getService(), getServiceInterface()});
		}
		catch (NoSuchMethodException ex) {
			// fall back to Hessian 2.x (without service interface argument)
			Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class});
			checkService();
			checkServiceInterface();
			this.skeleton = (HessianSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
		}
	}

	/**
	 * Process the incoming Hessian request and create a Hessian response.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HessianInput in = new HessianInput(request.getInputStream());
		HessianOutput out = new HessianOutput(response.getOutputStream());
		try {
		  this.skeleton.invoke(in, out);
		}
		catch (Exception ex) {
			throw ex;
		}
		catch (Error ex) {
			throw ex;
		}
		catch (Throwable ex) {
		  throw new ServletException(ex);
		}
		return null;
	}

}
