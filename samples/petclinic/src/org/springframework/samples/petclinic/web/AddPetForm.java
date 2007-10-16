
package org.springframework.samples.petclinic.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * JavaBean form controller that is used to add a new <code>Pet</code> to the
 * system.
 *
 * @author Ken Krebs
 * @author Mark Fisher
 */
@RequestMapping("/addPet.htm")
public class AddPetForm extends AbstractClinicForm {

	public AddPetForm() {
		setCommandName("pet");
		// need a session to hold the formBackingObject
		setSessionForm(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request) throws ServletException {
		Map refData = new HashMap();
		refData.put("types", getClinic().getPetTypes());
		return refData;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		super.initBinder(request, binder);
		binder.registerCustomEditor(PetType.class, new PetTypeEditor(getClinic().getPetTypes()));
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		Owner owner = getClinic().loadOwner(ServletRequestUtils.getRequiredIntParameter(request, "ownerId"));
		Pet pet = new Pet();
		owner.addPet(pet);
		return pet;
	}

	/** Method inserts a new Pet */
	@Override
	protected ModelAndView onSubmit(Object command) throws ServletException {
		Pet pet = (Pet) command;
		// delegate the insert to the Business layer
		getClinic().storePet(pet);
		return new ModelAndView(getSuccessView(), "ownerId", pet.getOwner().getId());
	}

	@Override
	protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return disallowDuplicateFormSubmission(request, response);
	}

}
