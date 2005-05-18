package org.springframework.web.flow.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.springframework.web.flow.execution.servlet.ServletEvent;

/**
 * A client flow event originating from a Struts environment. Provides access
 * to the Struts ActionMapping and ActionForm objects, as well as all
 * HttpServletRequest and HttpServletResponse information.
 * 
 * @author Keith Donald
 */
public class StrutsEvent extends ServletEvent {

	/**
	 * The Struts action mapping.
	 */
	private ActionMapping actionMapping;

	/**
	 * The Struts action form.
	 */
	private ActionForm actionForm;

	/**
	 * Creates a new Struts event.
	 * @param actionMapping the action mapping
	 * @param actionForm the action form
	 * @param request the request
	 * @param response the response
	 */
	public StrutsEvent(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
		this.actionMapping = actionMapping;
		this.actionForm = actionForm;
	}

	/**
	 * Creates a new Struts event.
	 * @param actionMapping the action mapping
	 * @param actionForm the action form
	 * @param request the request
	 * @param response the response
	 * @param eventIdParameterName name of the event id parameter in the request
	 * @param eventIdAttributeName name of the event id attribute in the request
	 * @param currentStateIdParameterName name of the current state id parameter
	 *        in the request
	 * @param parameterValueDelimiter delimiter used when a parameter value is
	 *        sent as part of the name of a request parameter (e.g. "_eventId_value=bar")
	 */
	public StrutsEvent(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
			HttpServletResponse response, String eventIdParameterName, String eventIdAttributeName,
			String currentStateIdParameterName, String parameterValueDelimiter) {
		super(request, response, eventIdParameterName, eventIdAttributeName, currentStateIdParameterName,
				parameterValueDelimiter);
		this.actionMapping = actionMapping;
		this.actionForm = actionForm;
	}

	/**
	 * Returns the action form.
	 */
	public ActionForm getActionForm() {
		return actionForm;
	}

	/**
	 * Returns the action mapping.
	 */
	public ActionMapping getActionMapping() {
		return actionMapping;
	}
}