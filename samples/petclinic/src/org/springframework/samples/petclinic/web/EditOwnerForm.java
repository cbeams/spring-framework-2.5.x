package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.FormAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.FormStatus;

/**
 * JavaBean Form controller that is used to edit an existing <code>Owner</code>.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/editOwner.do")
@FormAttributes("owner")
public class EditOwnerForm {

	private final Clinic clinic;

	@Autowired
	public EditOwnerForm(Clinic clinic) {
		this.clinic = clinic;
	}

	@RequestMapping(type = "GET")
	public String setupForm(@RequestParam("ownerId") int ownerId, ModelMap model) {
		Owner owner = this.clinic.loadOwner(ownerId);
		model.addObject("owner", owner);
		return "ownerForm";
	}

	@RequestMapping(type = "POST")
	public String processSubmit(@ModelAttribute("owner") Owner owner, BindingResult result, FormStatus status) {
		new OwnerValidator().validate(owner, result);
		if (result.hasErrors()) {
			return "ownerForm";
		}
		else {
			this.clinic.storeOwner(owner);
			status.setComplete();
			return "redirect:owner.do?ownerId=" + owner.getId();
		}
	}

}
