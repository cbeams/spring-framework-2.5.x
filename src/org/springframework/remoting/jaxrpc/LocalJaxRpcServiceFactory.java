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

package org.springframework.remoting.jaxrpc;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;

/**
 * Factory for locally defined JAX-RPC Service references.
 * Uses a JAX-RPC ServiceFactory underneath.
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see javax.xml.rpc.ServiceFactory
 * @see javax.xml.rpc.Service
 */
public class LocalJaxRpcServiceFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class serviceFactoryClass;

	private URL wsdlDocumentUrl;

	private String namespaceUri;

	private String serviceName;


	/**
	 * Set the ServiceFactory class to use, for example
	 * "org.apache.axis.client.ServiceFactory".
	 * <p>Does not need to be set if the JAX-RPC implementation has registered
	 * itself with the JAX-RPC system property "SERVICEFACTORY_PROPERTY".
	 * @see javax.xml.rpc.ServiceFactory
	 */
	public void setServiceFactoryClass(Class serviceFactoryClass) {
		if (serviceFactoryClass != null && !ServiceFactory.class.isAssignableFrom(serviceFactoryClass)) {
			throw new IllegalArgumentException("serviceFactoryClass must implement javax.xml.rpc.ServiceFactory");
		}
		this.serviceFactoryClass = serviceFactoryClass;
	}

	/**
	 * Return the ServiceFactory class to use, or null if default.
	 */
	public Class getServiceFactoryClass() {
		return serviceFactoryClass;
	}

	/**
	 * Set the URL of the WSDL document that describes the service.
	 */
	public void setWsdlDocumentUrl(URL wsdlDocumentUrl) {
		this.wsdlDocumentUrl = wsdlDocumentUrl;
	}

	/**
	 * Return the URL of the WSDL document that describes the service.
	 */
	public URL getWsdlDocumentUrl() {
		return wsdlDocumentUrl;
	}

	/**
	 * Set the namespace URI of the service.
	 * Corresponds to the WSDL "targetNamespace".
	 */
	public void setNamespaceUri(String namespaceUri) {
		this.namespaceUri = namespaceUri;
	}

	/**
	 * Return the namespace URI of the service.
	 */
	public String getNamespaceUri() {
		return namespaceUri;
	}

	/**
	 * Set the name of the service.
	 * Corresponds to the "wsdl:service" name.
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Return the name of the service.
	 */
	public String getServiceName() {
		return serviceName;
	}


	/**
	 * Return a QName for the given name,* relative to the namespace URI
	 * of this factory, if given.
	 * @see #setNamespaceUri
	 */
	public QName getQName(String name) {
		return (this.namespaceUri != null) ? new QName(this.namespaceUri, name) : new QName(this.serviceName);
	}

	/**
	 * Create a JAX-RPC ServiceFactory, either of the specified class
	 * or the default.
	 * @see #setServiceFactoryClass
	 */
	public ServiceFactory createServiceFactory() throws ServiceException {
		if (this.serviceFactoryClass != null) {
			return (ServiceFactory) BeanUtils.instantiateClass(this.serviceFactoryClass);
		}
		else {
			return ServiceFactory.newInstance();
		}
	}

	/**
	 * Create a JAX-RPC Service according to the parameters of this factory.
	 * @see #setServiceName
	 * @see #setWsdlDocumentUrl
	 */
	public Service createJaxRpcService() throws ServiceException {
		if (this.serviceName == null) {
			throw new IllegalArgumentException("serviceName is required");
		}
		ServiceFactory serviceFactory = createServiceFactory();
		QName serviceQName = getQName(this.serviceName);
		return (this.wsdlDocumentUrl != null) ?
				serviceFactory.createService(this.wsdlDocumentUrl, serviceQName) :
				serviceFactory.createService(serviceQName);
	}

}
