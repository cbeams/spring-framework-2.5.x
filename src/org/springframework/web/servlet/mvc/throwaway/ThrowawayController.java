package org.springframework.web.servlet.mvc.throwaway;

import org.springframework.web.servlet.ModelAndView;

/**
 * ThrowawayController is an alternative to Spring's default Controller interface,
 * for executable per-request command instances that are not aware of the Servlet API.
 * In contrast to Controller, implementing beans are not supposed to be defined as
 * Servlet/Struts-style singletons that process a HttpServletRequest but rather as
 * WebWork/Maverick-style prototypes that get populated with request parameters,
 * executed to determine a view, and thrown away afterwards.
 *
 * <p>The main advantage of this controller programming model is that controllers
 * are testable without HttpServletRequest/HttpServletResponse mocks, just like
 * WebWork actions. They are still web UI workflow controllers: Spring does not
 * aim for the arguably hard-to-achieve reusability of such controllers in non-web
 * environments, as XWork does (the generic command framework from WebWork2)
 * but just for ease of testing.
 *
 * <p>A ThrowawayController differs from the command notion of Base- respectively
 * AbstractCommandController in that a ThrowawayController is an <i>executable</i>
 * command that contains workflow logic to determine the next view to render,
 * while BaseCommandController treats commands as plain parameter holders.
 *
 * <p>If binding request parameters to this controller fails, a fatal BindException
 * will be thrown. If you want to react to binding errors and/or add validation
 * errors through a Validator or custom validation code, consider implementing a
 * ValidatableThrowawayController.
 *
 * <p>If you need access to the HttpServletRequest and/or HttpServletResponse,
 * consider implementing Controller or deriving from AbstractCommandController.
 * ThrowawayController is specifically intended for controllers that are not aware
 * of the Servlet API at all. Accordingly, if you need to handle session form objects
 * or even wizard forms, consider the corresponding Controller subclasses.
 *
 * @author Juergen Hoeller
 * @since 08.12.2003
 * @see ValidatableThrowawayController
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.servlet.mvc.AbstractCommandController
 */
public interface ThrowawayController {

	/**
	 * Execute this controller according to its bean properties.
	 * Gets invoked after a new instance of the controller has been populated with request
	 * parameters. Is supposed to return a ModelAndView in any case, as it is not able to
	 * generate a response itself.
	 * @return a ModelAndView to render
	 * @throws Exception in case of errors
	 */
	ModelAndView execute() throws Exception;

}
