package org.springframework.samples.petclinic.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * JavaBean Form controller that is used to search for <code>Owner</code>s by
 * last name.
 *
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/findOwners.do")
public class FindOwnersForm {

	@Autowired
	private Clinic clinic;

	@RequestMapping(type = "GET")
	protected String setupForm(ModelMap model) {
		model.addObject("owner", new Owner());
		return "findOwners";
	}

	@RequestMapping(type = "POST")
	protected String processSubmit(Owner owner, BindingResult result, ModelMap model) {
		// find owners by last name
		Collection<Owner> results = this.clinic.findOwners(owner.getLastName());
		if (results.size() < 1) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "findOwners";
		}
		if (results.size() > 1) {
			// multiple owners found
			model.addObject("selections", results);
			return "owners";
		}
		else {
			// 1 owner found
			owner = results.iterator().next();
			return "redirect:owner.do?ownerId=" + owner.getId();
		}
	}

}
