package org.springframework.samples.itemlist.web.flow;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.MutableFlowModel;

public class AddItemAction implements Action {
	public String execute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		//check to ensure the incoming request is within the active transaction
		if (!model.inTransaction(request, true)) {
			//the transaction was not valid so cannot continue normal
			// processing
			return "txError";
		}
		List list = (List)model.getAttribute("list");
		if (list == null) {
			list = new ArrayList();
			model.setAttribute("list", list);
		}
		String data = request.getParameter("data");
		if (data != null && data.length() > 0) {
			list.add(data);
		}
		//add a bit of artificial think time
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		}
		return "success";
	}
}