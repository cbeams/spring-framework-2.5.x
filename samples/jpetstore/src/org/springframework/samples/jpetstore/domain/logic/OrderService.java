package org.springframework.samples.jpetstore.domain.logic;

import org.springframework.samples.jpetstore.domain.Order;

/**
 * Separate OrderService interface, implemented by PetStoreImpl
 * in addition to PetStoreFacade.
 *
 * <p>Mainly targeted at usage as remote service interface,
 * just exposing the getOrder method.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 * @see PetStoreFacade
 * @see PetStoreImpl
 * @see org.springframework.samples.jpetstore.service.RemoteOrderService
 * @see org.springframework.samples.jpetstore.service.server.JaxRpcOrderService
 */
public interface OrderService {

	Order getOrder(int orderId);

}
