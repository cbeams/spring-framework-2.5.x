package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 */
@Controller
public class ClinicController {

	@Autowired
	private Clinic clinic;


	/**
	 * Custom handler for the welcome view.
	 * <p>
	 * Note that this handler relies on the RequestToViewNameTranslator to come up
	 * with a view name based on the request URL: "/welcome.do" -> "welcome".
	 */
	@RequestMapping("/welcome.do")
	public void welcomeHandler() {
	}

	/**
	 * Custom handler for vets display.
	 * <p>
	 * Note that this handler returns a plain ModelMap object instead of a
	 * ModelAndView, also leveraging convention-based model attribute names. It
	 * relies on the RequestToViewNameTranslator to come up with a view name
	 * based on the request URL: "/vetsl" -> "vets", plus configured "View"
	 * suffix -> "vetsView".
	 *
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/vets.do")
	public ModelMap vetsHandler() {
		return new ModelMap(this.clinic.getVets());
	}

	/**
	 * Custom handler for owner display.
	 * <p>
	 * Note that this handler usually returns a ModelAndView object without
	 * view, also leveraging convention-based model attribute names. It relies
	 * on the RequestToViewNameTranslator to come up with a view name based on
	 * the request URL: "/ownerl" -> "owner", plus configured "View" suffix ->
	 * "ownerView".
	 *
	 * @param ownerId the id of the owner to show
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/owner.do")
	public ModelMap ownerHandler(@RequestParam("ownerId") int ownerId) {
		return new ModelMap(this.clinic.loadOwner(ownerId));
	}

}
