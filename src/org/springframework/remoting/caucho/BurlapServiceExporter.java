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

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.server.BurlapSkeleton;

import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.beans.factory.InitializingBean;

/**
 * Web controller that exports the specified service bean as Burlap service
 * endpoint, accessible via a Burlap proxy.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>This exporter will work with both Burlap 2.x and 3.x (respectively
 * Resin 2.x and 3.x), auto-detecting the corresponding skeleton class.
 *
 * <p>Note: Burlap services exported with this class can be accessed by
 * any Burlap client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see BurlapProxyFactoryBean
 */
public class BurlapServiceExporter extends RemoteExporter implements Controller, InitializingBean {

	private BurlapSkeleton skeleton;

	public void afterPropertiesSet() throws Exception {
		try {
			// try Burlap 3.x (with service interface argument)
			Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
			checkService();
			checkServiceInterface();
			this.skeleton = (BurlapSkeleton) ctor.newInstance(new Object[] {getService(), getServiceInterface()});
		}
		catch (NoSuchMethodException ex) {
			// fall back to Burlap 2.x (without service interface argument)
			Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class});
			this.skeleton = (BurlapSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
		}
	}

	/**
	 * Process the incoming Burlap request and create a Burlap response.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BurlapInput in = new BurlapInput(request.getInputStream());
		BurlapOutput out = new BurlapOutput(response.getOutputStream());
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
