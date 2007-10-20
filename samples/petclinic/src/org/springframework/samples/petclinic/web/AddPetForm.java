package org.springframework.samples.petclinic.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.validation.PetValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.FormStatus;

/**
 * JavaBean form controller that is used to add a new <code>Pet</code> to the
 * system.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/addPet.do")
@SessionAttributes("pet")
public class AddPetForm {

	private final Clinic clinic;

	@Autowired
	public AddPetForm(Clinic clinic) {
		this.clinic = clinic;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.clinic.getPetTypes();
	}

	@RequestMapping(type = "GET")
	public String setupForm(@RequestParam("ownerId") int ownerId, ModelMap model) {
		Owner owner = this.clinic.loadOwner(ownerId);
		Pet pet = new Pet();
		owner.addPet(pet);
		model.addAttribute("pet", pet);
		return "petForm";
	}

	@RequestMapping(type = "POST")
	public String processSubmit(@ModelAttribute("pet") Pet pet, BindingResult result, FormStatus status) {
		new PetValidator().validate(pet, result);
		if (result.hasErrors()) {
			return "petForm";
		}
		else {
			this.clinic.storePet(pet);
			status.setComplete();
			return "redirect:owner.do?ownerId=" + pet.getOwner().getId();
		}
	}

}
