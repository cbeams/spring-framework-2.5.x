package org.springframework.samples.jpetstore.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.springframework.samples.jpetstore.domain.Order;

/**
 * RMI interface that matches OrderService, to be used as port interface for
 * JAX-RPC compliant service access (see "clientContext.xml"). Also needs to be
 * implemented by the service endpoint for JAX-RPC compliant export, i.e.
 * JaxRpcOrderService. RMI interfaces are required at the JAX-RPC level.
 *
 * <p>Client objects that access the service via JaxRpcPortClientInterceptor
 * respectively JaxRpcPortProxyFactoryBean can work with the plain OrderService
 * interface, specifying OrderService as "serviceInterface" and RemoteOrderService
 * as "portInterface".
 *
 * <p>Note: Neither this interface nor a wrapper class like JaxRpcOrderService
 * is necessary for working with Hessian, Burlap, or Spring's RMI invoker.
 * See "applicationContext.xml", "caucho-servlet.xml", and "clientContext.xml"
 * for details on setting those up in a non-intrusive manner.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 * @see org.springframework.samples.jpetstore.service.server.JaxRpcOrderService
 * @see org.springframework.samples.jpetstore.domain.logic.OrderService
 * @see org.springframework.remoting.jaxrpc.JaxRpcPortClientInterceptor#setPortInterface
 * @see org.springframework.remoting.jaxrpc.JaxRpcPortClientInterceptor#setServiceInterface
 */
public interface RemoteOrderService extends Remote {

	Order getOrder(int orderId) throws RemoteException;

}
