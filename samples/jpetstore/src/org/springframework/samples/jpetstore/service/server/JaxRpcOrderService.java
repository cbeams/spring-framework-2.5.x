package org.springframework.samples.jpetstore.service.server;

import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.logic.OrderService;
import org.springframework.samples.jpetstore.service.RemoteOrderService;

/**
 * JAX-RPC compliant RemoteOrderService implementation that simply delegates
 * to the OrderService implementation in the root web application context.
 *
 * <p>This wrapper class is necessary because JAX-RPC requires working with
 * RMI interfaces. If an existing service needs to be exported, a wrapper that
 * extends ServletEndpointSupport for simple application context access is
 * the simplest JAX-RPC compliant way.
 *
 * <p>This is the class registered with the server-side JAX-RPC implementation.
 * In the case of Axis, this happens in "server-config.wsdd" respectively via
 * deployment calls. The Web Service tool manages the lifecycle of instances
 * of this class: A Spring application context can just be accessed here.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 */
public class JaxRpcOrderService extends ServletEndpointSupport
		implements RemoteOrderService, OrderService {

	private OrderService orderService;

	protected void onInit() {
		this.orderService = (OrderService) getWebApplicationContext().getBean("petStore");
	}

	public Order getOrder(int orderId) {
		return this.orderService.getOrder(orderId);
	}

}
