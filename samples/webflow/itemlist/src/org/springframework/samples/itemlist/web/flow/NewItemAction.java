package org.springframework.samples.itemlist.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.MutableFlowModel;

public class NewItemAction implements Action {
	public String execute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		//begin transactional processing
		model.beginTransaction();
		return "success";
	}
}