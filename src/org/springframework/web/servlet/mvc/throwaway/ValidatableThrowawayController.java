package org.springframework.web.servlet.mvc.throwaway;

import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Alternative to ThrowawayController that is aware of binding errors and allows
 * for customization of the binding process. This is a different interface as it
 * specifies an Errors-aware execute method: Bean property setters are not used
 * to avoid name conflicts with request parameters on binding. Of course,
 * implementing classes are free to use specific setters for their purposes.
 *
 * <p>ValidatableThrowawayController is slightly harder to test than ThrowawayController
 * as it needs a mock DataBinder: DataBinder itself will be just fine for that purpose,
 * while ServletRequestDataBinder will be used when executed within the web MVC framework.
 *
 * <p>If you need access to the HttpServletRequest and/or HttpServletResponse,
 * consider implementing Controller or deriving from AbstractCommandController.
 * ValidatableThrowawayController is aware of Spring's binding process but is still
 * specifically intended for controllers that are not aware of the Servlet API at all.
 * Accordingly, if you need to handle session form objects or even wizard forms,
 * consider the corresponding Controller subclasses.
 *
 * @author Juergen Hoeller
 * @since 08.12.2003
 */
public interface ValidatableThrowawayController {

	/**
	 * Return the name of the model attribute that will contain this controller
	 * instance. Also used for the name of the Errors attribute in the model.
	 * <p>Note: Will only be applied to the model if the execute method retrieves
	 * the model from the given validation errors holder.
	 * @return the name of the model attribute
	 * @see #execute
	 * @see org.springframework.validation.BindException#getModel
	 */
	String getName();

	/**
	 * Initialize the given binder instance, e.g. with custom editors.
	 * <p>This method allows you to register custom editors for certain fields of your
	 * throwaway controller class. For instance, you will be able to transform Date
	 * objects into a String pattern and back, in order to allow your JavaBeans to
	 * have Date properties and still be able to set and display them in for instance
	 * an HTML interface.
	 * @param binder new binder instance, to be used for binding parameters to the
	 * bean properties of this controller instance
	 * @throws Exception in case of invalid state or arguments
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 */
	void initBinder(DataBinder binder) throws Exception;

	/**
	 * Execute this controller according to its bean properties.
	 * Gets invoked after a new instance of the controller has been populated with request
	 * parameters. Is supposed to return a ModelAndView in any case, as it is not able to
	 * generate a response itself.
	 * <p>Can invoke <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the controller and the Errors instance, under the specified command name.
	 * @param errors the validation errors holder that resulted from binding
	 * @return a ModelAndView to render
	 * @throws Exception in case of errors
	 */
	ModelAndView execute(BindException errors) throws Exception;

}
