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

import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.util.NestedServletException;

/**
 * HTTP request handler that exports the specified service bean as
 * Hessian service endpoint, accessible via a Hessian proxy.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>This exporter will work with both Hessian 2.x and 3.x (respectively
 * Resin 2.x and 3.x), autodetecting the corresponding skeleton class.
 * As of Spring 2.0, it is also compatible with the new Hessian 2 protocol
 * (a.k.a. Hessian 3.0.20+), while remaining compatible with older versions.
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

	private static final boolean hessian2Available =
			ClassUtils.isPresent("com.caucho.hessian.io.Hessian2Input", HessianServiceExporter.class.getClassLoader());


	private SerializerFactory serializerFactory = new SerializerFactory();

	private HessianSkeletonInvoker skeletonInvoker;


	/**
	 * Specify the Hessian SerializerFactory to use.
	 * <p>This will typically be passed in as an inner bean definition
	 * of type <code>com.caucho.hessian.io.SerializerFactory</code>,
	 * with custom bean property values applied.
	 */
	public void setSerializerFactory(SerializerFactory serializerFactory) {
		this.serializerFactory = (serializerFactory != null ? serializerFactory : new SerializerFactory());
	}

	/**
	 * Set whether to send the Java collection type for each serialized
	 * collection. Default is "true".
	 */
	public void setSendCollectionType(boolean sendCollectionType) {
		this.serializerFactory.setSendCollectionType(sendCollectionType);
	}


	public void afterPropertiesSet() {
		prepare();
	}

	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		HessianSkeleton skeleton = null;

		try {
			try {
				// Try Hessian 3.x (with service interface argument).
				Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
				checkService();
				checkServiceInterface();
				skeleton = (HessianSkeleton)
						ctor.newInstance(new Object[] {getProxyForService(), getServiceInterface()});
			}
			catch (NoSuchMethodException ex) {
				// Fall back to Hessian 2.x (without service interface argument).
				Constructor ctor = HessianSkeleton.class.getConstructor(new Class[] {Object.class});
				skeleton = (HessianSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
			}
		}
		catch (Throwable ex) {
			throw new BeanInitializationException("Hessian skeleton initialization failed", ex);
		}

		if (hessian2Available) {
			// Hessian 2 (version 3.0.20+).
			this.skeletonInvoker = new Hessian2SkeletonInvoker(skeleton, this.serializerFactory);
		}
		else {
			// Hessian 1 (version 3.0.19-).
			this.skeletonInvoker = new Hessian1SkeletonInvoker(skeleton, this.serializerFactory);
		}
	}


	/**
	 * Processes the incoming Hessian request and creates a Hessian response.
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Assert.notNull(this.skeletonInvoker, "HessianServiceExporter has not been initialized");

		if (!"POST".equals(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(
					"POST", "HessianServiceExporter only supports POST requests");
		}

		try {
		  this.skeletonInvoker.invoke(request.getInputStream(), response.getOutputStream());
		}
		catch (Throwable ex) {
		  throw new NestedServletException("Hessian skeleton invocation failed", ex);
		}
	}

}
