package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.samples.jpetstore.domain.Category;
import org.springframework.util.PagedListHolder;

public class ViewCategoryAction extends BaseAction {

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    String categoryId = request.getParameter("categoryId");
    if (categoryId != null) {
      PagedListHolder productList = new PagedListHolder(getPetStore().getProductListByCategory(categoryId));
			productList.setPageSize(4);
			Category category = getPetStore().getCategory(categoryId);
			request.getSession().setAttribute("ViewProductAction_productList", productList);
			request.getSession().setAttribute("ViewProductAction_category", category);
			request.setAttribute("productList", productList);
      request.setAttribute("category", category);
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
			request.setAttribute("productList", productList);
      request.setAttribute("category", category);
    }
    return mapping.findForward("success");
  }

}
