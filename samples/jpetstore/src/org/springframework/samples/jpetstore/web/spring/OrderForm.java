package org.springframework.samples.jpetstore.web.spring;

import org.springframework.samples.jpetstore.domain.Order;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class OrderForm {

	private final Order order = new Order();

	private boolean shippingAddressRequired;

	private boolean confirmed;

	public Order getOrder() {
		return order;
	}

	public void setShippingAddressRequired(boolean shippingAddressRequired) {
		this.shippingAddressRequired = shippingAddressRequired;
	}

	public boolean isShippingAddressRequired() {
		return shippingAddressRequired;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

}
