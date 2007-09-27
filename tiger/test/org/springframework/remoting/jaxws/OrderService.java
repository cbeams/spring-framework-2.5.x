package org.springframework.remoting.jaxws;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface OrderService {

	String getOrder(int id);
}
