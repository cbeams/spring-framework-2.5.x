package org.springframework.autobuilds.jpetstore.web.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.autobuilds.jpetstore.domain.Category;
import org.springframework.autobuilds.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class ViewCategoryController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		String categoryId = request.getParameter("categoryId");
		if (categoryId != null) {
			PagedListHolder productList = new PagedListHolder(this.petStore.getProductListByCategory(categoryId));
			productList.setPageSize(4);
			Category category = this.petStore.getCategory(categoryId);
			request.getSession().setAttribute("ViewProductAction_productList", productList);
			request.getSession().setAttribute("ViewProductAction_category", category);
			model.put("productList", productList);
			model.put("category", category);
		}
		else {
			PagedListHolder productList = (PagedListHolder) request.getSession().getAttribute("ViewProductAction_productList");
			Category category = (Category) request.getSession().getAttribute("ViewProductAction_category");
			String page = request.getParameter("page");
			if ("next".equals(page)) {
				productList.nextPage();
			} else if ("previous".equals(page)) {
				productList.previousPage();
			}
			model.put("productList", productList);
			model.put("category", category);
		}
		return new ModelAndView("Category", model);
	}

}
