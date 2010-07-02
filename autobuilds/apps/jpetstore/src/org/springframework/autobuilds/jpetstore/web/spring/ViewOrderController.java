package org.springframework.autobuilds.jpetstore.web.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.autobuilds.jpetstore.domain.Order;
import org.springframework.autobuilds.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class ViewOrderController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int orderId = Integer.parseInt(request.getParameter("orderId"));
		Order order = this.petStore.getOrder(orderId);
		UserSession userSession = (UserSession) WebUtils.getSessionAttribute(request, "userSession");
		if (userSession.getAccount().getUsername().equals(order.getUsername())) {
			return new ModelAndView("ViewOrder", "order", order);
		}
		else {
			return new ModelAndView("Error", "message", "You may only view your own orders.");
		}
	}

}
