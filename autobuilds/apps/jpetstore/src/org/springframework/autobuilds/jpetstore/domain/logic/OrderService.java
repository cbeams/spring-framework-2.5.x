package org.springframework.autobuilds.jpetstore.domain.logic;

import org.springframework.autobuilds.jpetstore.domain.Order;

/**
 * Separate OrderService interface, implemented by PetStoreImpl
 * in addition to PetStoreFacade.
 *
 * <p>Mainly targetted at usage as remote service interface.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 * @see PetStoreFacade
 * @see PetStoreImpl
 * @see org.springframework.autobuilds.jpetstore.service.RemoteOrderService
 * @see org.springframework.autobuilds.jpetstore.service.server.JaxRpcOrderService
 */
public interface OrderService {

	Order getOrder(int orderId);

}
