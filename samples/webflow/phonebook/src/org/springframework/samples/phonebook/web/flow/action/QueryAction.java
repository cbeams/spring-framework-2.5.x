package org.springframework.samples.phonebook.web.flow.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.phonebook.domain.PhoneBook;
import org.springframework.samples.phonebook.domain.PhoneBookQuery;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.MutableFlowModel;

public class QueryAction implements Action {

	private PhoneBook phoneBook;

	public void setPhoneBook(PhoneBook phoneBook) {
		this.phoneBook = phoneBook;
	}

	public String execute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		PhoneBookQuery query = (PhoneBookQuery)model.getAttribute("query");
		model.setAttribute("persons", phoneBook.query(query));
		return "success";
	}
}