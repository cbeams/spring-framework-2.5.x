/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.xml.rpc.Service;

/**
 * Callback interface for post-processing a JAX-RPC Service.
 * Implementations can be registered with LocalJaxRpcServiceFactory
 * or one of its subclasses: LocalJaxRpcServiceFactoryBean,
 * JaxRpcPortClientInterceptor, JaxRpcPortProxyFactoryBean.
 *
 * <p>Useful, for example, to register custom type mappings.
 * See the JPetStore sample app for an example: It features a
 * BeanMappingServicePostProcessor class that registers
 * Axis-specific bean mappings for JPetStore's domain objects.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see LocalJaxRpcServiceFactory#setServicePostProcessors
 * @see LocalJaxRpcServiceFactoryBean#setServicePostProcessors
 * @see JaxRpcPortClientInterceptor#setServicePostProcessors
 * @see JaxRpcPortProxyFactoryBean#setServicePostProcessors
 * @see javax.xml.rpc.Service#getTypeMappingRegistry
 */
public interface JaxRpcServicePostProcessor {

	/**
	 * Post-process the given JAX-RPC Service.
	 * @param service the current JAX-RPC Service
	 * (can be cast to an implementation-specific class if necessary)
	 */
	void postProcessJaxRpcService(Service service);

}
