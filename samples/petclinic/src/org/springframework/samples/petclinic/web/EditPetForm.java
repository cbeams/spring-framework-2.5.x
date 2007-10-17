package org.springframework.samples.petclinic.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.validation.PetValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.FormAttributes;
import org.springframework.web.bind.support.FormStatus;

/**
 * JavaBean Form controller that is used to edit an existing <code>Pet</code>.
 *
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/editPet.do")
@FormAttributes("pet")
public class EditPetForm {

	@Autowired
	private Clinic clinic;

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.clinic.getPetTypes();
	}

	@RequestMapping(type = "GET")
	public String setupForm(@RequestParam("petId") int petId, ModelMap model) {
		Pet pet = this.clinic.loadPet(petId);
		model.addObject("pet", pet);
		return "petForm";
	}

	@RequestMapping(type = "POST")
	protected String processSubmit(Pet pet, BindingResult result, FormStatus status) {
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
