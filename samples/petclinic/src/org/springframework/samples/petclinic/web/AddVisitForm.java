package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.samples.petclinic.validation.VisitValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.FormStatus;

/**
 * JavaBean form controller that is used to add a new <code>Visit</code> to
 * the system.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/addVisit.do")
@SessionAttributes("visit")
public class AddVisitForm {

	private final Clinic clinic;

	@Autowired
	public AddVisitForm(Clinic clinic) {
		this.clinic = clinic;
	}

	@RequestMapping(type = "GET")
	public String setupForm(@RequestParam("petId") int petId, ModelMap model) {
		Pet pet = this.clinic.loadPet(petId);
		Visit visit = new Visit();
		pet.addVisit(visit);
		model.addAttribute("visit", visit);
		return "visitForm";
	}

	@RequestMapping(type = "POST")
	public String processSubmit(@ModelAttribute("visit") Visit visit, BindingResult result, FormStatus status) {
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
