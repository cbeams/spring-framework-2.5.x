package org.springframework.web.portlet;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import org.springframework.validation.BindException;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.support.PortletController;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>First cut of a FormController for Portlets.</p>
 *
 * <p>
 * 	<ul>
 * 		<li>TODO: Refactor this to have a sensible heirachy</li>
 * 		<li>TODO: Add session form support</li>
 * 		<li>TODO: Re-add hooks for easier customization in the same way the Web framwork does it.</li>
 * 	<ul>
 * </p>
 *
 * @author nl Nick Lothian
 *
 */
public class SimplePortletFormController implements PortletController {

    public static final String IS_FORM_SUBMISSION = "ISFORMSUBMIT"; // we can't make this the classname as there is often a limit on the max length of all parameters

    private Class commandClass;
    private String commandName;
    private Validator[] validators;
    private boolean validateOnBinding;

    private String formView;

    private String successView;

    private MessageCodesResolver messageCodesResolver;

    /**
     * @return Returns the messageCodesResolver.
     */
    public MessageCodesResolver getMessageCodesResolver() {
        return messageCodesResolver;
    }
    /**
     * @param messageCodesResolver The messageCodesResolver to set.
     */
    public void setMessageCodesResolver(
            MessageCodesResolver messageCodesResolver) {
        this.messageCodesResolver = messageCodesResolver;
    }
	/**
	 * Set the name of the command in the model.
	 * The command object will be included in the model under this name.
	 */
	public final void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Return the name of the command in the model.
	 */
	protected final String getCommandName() {
		return this.commandName;
	}

	/**
	 * Set the command class for this controller.
	 * An instance of this class gets populated and validated on each request.
	 */
	public final void setCommandClass(Class commandClass) {
		this.commandClass = commandClass;
	}

	/**
	 * Return the command class for this controller.
	 */
	protected final Class getCommandClass() {
		return this.commandClass;
	}

	/**
	 * Set the primary Validator for this controller.
	 * The Validator must support the specified command class.
	 */
	public final void setValidators(Validator[] validators) {
		this.validators = validators;
	}

	/**
	 * Return the Validators for this controller.
	 */
	protected final Validator[] getValidators() {
		return validators;
	}

	/**
	 * Set the Validators for this controller.
	 * The Validator must support the specified command class.
	 */
	public final void setValidator(Validator validator) {
		this.validators = new Validator[] {validator};
	}

	/**
	 * Return the primary Validator for this controller.
	 */
	protected final Validator getValidator() {
		return (validators != null && validators.length > 0 ? validators[0] : null);
	}

	/**
	 * Set if the Validator should get applied when binding.
	 */
	public final void setValidateOnBinding(boolean validateOnBinding) {
		this.validateOnBinding = validateOnBinding;
	}

	/**
	 * Return if the Validator should get applied when binding.
	 */
	protected final boolean isValidateOnBinding() {
		return validateOnBinding;
	}

	/**
	 * Set the name of the view that should be used for form display.
	 */
	public final void setFormView(String formView) {
		this.formView = formView;
	}

	/**
	 * Return the name of the view that should be used for form display.
	 */
	protected final String getFormView() {
		return this.formView;
	}

	/**
	 * Set the name of the view that should be shown on successful submit.
	 */
	public final void setSuccessView(String successView) {
		this.successView = successView;
	}

	/**
	 * Return the name of the view that should be shown on successful submit.
	 */
	protected final String getSuccessView() {
		return this.successView;
	}

    /**
     * @see org.springframework.web.portlet.support.PortletController#handleRequest(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void handleRequest(ActionRequest request, ActionResponse response) throws Exception {
        // The Portlet actionURL/renderURL is difficult to integrate into the Spring MVC framework
        // In theory, validation should probably be done here, and then any errors
        // should be passed to the render phase as attribute. However, the Portlet
        // specification requires the render phase to be a separate HTTP request.
        //
        // What we do here is grab the parameters and then pass them on to the render phase
        // and let that handle the validation. We add a IS_FORM_SUBMISSION parameter so that
        // the render phase can detect it is coming from an action phase and validation is required.
        //
        Map parameterMap = request.getParameterMap();
        response.setRenderParameters(parameterMap);

        response.setRenderParameter(IS_FORM_SUBMISSION, Boolean.TRUE.toString());
    }

    /**
     * @see org.springframework.web.portlet.support.PortletController#handleRequest(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public ModelAndView handleRequest(RenderRequest request, RenderResponse response) throws Exception {
        Object command = formBackingObject(request);
        PortletRequestDataBinder binder = bindAndValidate(request, command);
        if (isFormSubmission(request)) {
            return processFormSubmission(request, response, command, binder.getErrors());
        } else {
            return showForm(request, getFormView(), binder.getErrors());
        }
    }

	protected ModelAndView processFormSubmission(PortletRequest request, PortletResponse response, Object command, BindException errors) throws Exception {
		if (errors.hasErrors() || isFormChangeRequest(request)) {
			return showForm(request, getFormView(), errors);
		}
		else {
			return onSubmit(command, errors);
		}
	}

	protected ModelAndView showForm(PortletRequest request, String viewName, BindException errors) {
	    Map model = errors.getModel();

	    return new ModelAndView(viewName, model);
	}

	protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
		ModelAndView mv = onSubmit(command);
		if (mv != null) {
			// simplest onSubmit version implemented in custom subclass
		    // The Web version doesn't add the errors to the model - I'm not sure
		    // how that works
		    Map errorModel = errors.getModel();
		    mv.getModel().putAll(errorModel);

			return mv;
		}
		else {
			// default behavior: render success view
			if (getSuccessView() == null) {
				throw new ServletException("successView isn't set");
			}
			return new ModelAndView(getSuccessView(), errors.getModel());
		}
	}



	public ModelAndView onSubmit(Object command) {
	    return null;
	}


    /**
     * @param request
     * @return
     */
    private boolean isFormSubmission(RenderRequest request) {
        return Boolean.TRUE.toString().equals(request.getParameter(IS_FORM_SUBMISSION));
    }

	protected boolean isFormChangeRequest(PortletRequest request) {
		return false;
	}

	protected boolean suppressValidation(PortletRequest request) {
		return false;
	}

	protected PortletRequestDataBinder createBinder(PortletRequest request, Object command) throws Exception {
	    PortletRequestDataBinder binder = new PortletRequestDataBinder(command, getCommandName());

        if (this.messageCodesResolver != null) {
            binder.setMessageCodesResolver(this.messageCodesResolver);
        }

        initBinder(request, binder);
        return binder;
    }

	protected void initBinder(PortletRequest request, PortletRequestDataBinder binder) throws Exception {
	}

	protected final PortletRequestDataBinder bindAndValidate(PortletRequest request, Object command) throws Exception {
	    PortletRequestDataBinder binder = createBinder(request, command);
        binder.bind(request);
        //onBind(request, command, binder.getErrors());
        if (this.validators != null && isValidateOnBinding() && !suppressValidation(request)) {
            for (int i = 0; i < this.validators.length; i++) {
                ValidationUtils.invokeValidator(this.validators[i], command,
                        binder.getErrors());
            }
        }
        //onBindAndValidate(request, command, binder.getErrors());
        return binder;
    }


	protected Object formBackingObject(PortletRequest request) throws Exception {
		return createCommand();
	}

	protected final Object createCommand() throws InstantiationException, IllegalAccessException {
		if (this.commandClass == null) {
			throw new IllegalStateException("Cannot create command without commandClass being set - " +
																			"either set commandClass or override formBackingObject");
		}
		return this.commandClass.newInstance();
	}

	protected void initApplicationContext() {
		if (this.validators != null) {
			for (int i = 0; i < this.validators.length; i++) {
				if (this.commandClass != null && !this.validators[i].supports(this.commandClass))
					throw new IllegalArgumentException("Validator [" + this.validators[i] +
																						 "] does not support command class [" +
																						 this.commandClass.getName() + "]");
			}
		}
	}

}
