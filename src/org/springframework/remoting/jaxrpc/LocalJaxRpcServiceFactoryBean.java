package org.springframework.remoting.jaxrpc;

import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean for locally defined JAX-RPC Service references.
 * Uses LocalJaxRpcServiceFactory's facilities underneath.
 *
 * <p>Alternatively, JAX-RPC Service references can be looked up
 * in the JNDI environment of the J2EE container.
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see javax.xml.rpc.Service
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see JaxRpcPortProxyFactoryBean
 */
public class LocalJaxRpcServiceFactoryBean extends LocalJaxRpcServiceFactory
		implements FactoryBean, InitializingBean {

	private Service service;

	public void afterPropertiesSet() throws ServiceException {
		this.service = createJaxRpcService();
	}

	public Object getObject() throws Exception {
		return this.service;
	}

	public Class getObjectType() {
		return (this.service != null ? this.service.getClass() : Service.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
