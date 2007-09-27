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

package org.springframework.remoting.jaxws;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Simple exporter for JAX-WS services, autodetecting annotation service beans
 * (through the JAX-WS {@link javax.jws.WebService} annotation) and exporting
 * them with a configured base address (by default "http://localhost:8080/").
 * The full address for each service will consist of the base address with
 * the service name appended (e.g. "http://localhost:8080/OrderService").
 *
 * <p>Note that this exporter will only work if the JAX-WS runtime actually
 * supports publishing with an address argument, i.e. if the JAX-WS runtime
 * ships an internal HTTP server. This is the case with the JAX-WS runtime
 * that's inclued in Sun's JDK 1.6 but not with the standalone JAX-WS 2.1 RI.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see javax.xml.ws.Endpoint#publish(String, Object)
 */
public class SimpleJaxWsServiceExporter implements BeanFactoryAware, DisposableBean {

	public static final String DEFAULT_BASE_ADDRESS = "http://localhost:8080/";


	private String baseAddress = DEFAULT_BASE_ADDRESS;

	private Executor executor;

	private Map<String, Object> endpointProperties;

	private final Set<Endpoint> publishedEndpoints = new LinkedHashSet<Endpoint>();


	public void setBaseAddress(String baseAddress) {
		this.baseAddress = baseAddress;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void setEndpointProperties(Map<String, Object> endpointProperties) {
		this.endpointProperties = endpointProperties;
	}


	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalStateException("SimpleJaxWsServiceExporter requires a ListableBeanFactory");
		}
		ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
		String[] beanNames = lbf.getBeanNamesForType(Object.class, false, false);
		for (String beanName : beanNames) {
			Class<?> type = lbf.getType(beanName);
			WebService annotation = type.getAnnotation(WebService.class);
			if (annotation != null) {
				Endpoint endpoint = Endpoint.create(lbf.getBean(beanName));
				if (this.executor != null) {
					endpoint.setExecutor(this.executor);
				}
				if (this.endpointProperties != null) {
					endpoint.setProperties(this.endpointProperties);
				}
				String fullAddress = this.baseAddress + annotation.serviceName();
				endpoint.publish(fullAddress);
				this.publishedEndpoints.add(endpoint);
			}
		}
	}

	public void destroy() {
		for (Endpoint endpoint : this.publishedEndpoints) {
			endpoint.stop();
		}
	}

}
