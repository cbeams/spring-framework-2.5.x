package org.springframework.remoting.jaxws;

import javax.jws.WebService;

@WebService(serviceName="OrderService", portName="OrderService", endpointInterface = "org.springframework.remoting.jaxws.OrderService")
public class OrderServiceImpl implements OrderService {

	public String getOrder(int id) {
		return "order " + id;
	}
}
