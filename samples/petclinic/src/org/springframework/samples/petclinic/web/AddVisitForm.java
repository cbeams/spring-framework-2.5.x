
package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.samples.petclinic.validation.VisitValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.FormAttributes;
import org.springframework.web.bind.support.FormStatus;

/**
 * JavaBean form controller that is used to add a new <code>Visit</code> to
 * the system.
 *
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/addVisit.do")
@FormAttributes("visit")
public class AddVisitForm {

	@Autowired
	private Clinic clinic;

	@RequestMapping(type = "GET")
	public String setupForm(@RequestParam("petId") int petId, ModelMap model) {
		Pet pet = this.clinic.loadPet(petId);
		Visit visit = new Visit();
		pet.addVisit(visit);
		model.addObject("visit", visit);
		return "visitForm";
	}

	@RequestMapping(type = "POST")
	protected String processSubmit(Visit visit, BindingResult result, FormStatus status) {
		new VisitValidator().validate(visit, result);
		if (result.hasErrors()) {
			return "visitForm";
		}
		else {
			this.clinic.storeVisit(visit);
			status.setComplete();
			return "redirect:owner.do?ownerId=" + visit.getPet().getOwner().getId();
		}
	}

}
