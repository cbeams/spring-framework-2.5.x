package org.springframework.samples.phonebook.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.phonebook.domain.Person;
import org.springframework.samples.phonebook.domain.PhoneBook;
import org.springframework.samples.phonebook.domain.UserId;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.MutableFlowModel;

public class GetPersonAction implements Action {

	private PhoneBook phoneBook;

	public void setPhoneBook(PhoneBook phoneBook) {
		this.phoneBook = phoneBook;
	}

	public String execute(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		UserId userId = (UserId)model.getAttribute("id");
		Person person = phoneBook.getPerson(userId);
		if (person != null) {
			model.setAttribute("person", person);
			return "success";
		}
		else {
			return "error";
		}
	}
}