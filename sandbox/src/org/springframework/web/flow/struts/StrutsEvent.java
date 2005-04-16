package org.springframework.web.flow.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

public class StrutsEvent extends HttpServletRequestEvent {

	/**
	 * The struts action mapping.
	 */
	private ActionMapping actionMapping;

	/**
	 * The struts action form.
	 */
	private ActionForm actionForm;

	/**
	 * @param actionMapping
	 * @param actionForm
	 * @param request
	 * @param response
	 */
	public StrutsEvent(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
		this.actionMapping = actionMapping;
		this.actionForm = actionForm;
	}

	/**
	 * @param actionMapping
	 * @param actionForm
	 * @param request
	 * @param response
	 * @param eventIdParameterName
	 * @param eventIdAttributeName
	 * @param currentStateIdParameterName
	 * @param parameterValueDelimiter
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
	 * @return the action form
	 */
	public ActionForm getActionForm() {
		return actionForm;
	}
	
	/**
	 * @return the action mapping
	 */
	public ActionMapping getActionMapping() {
		return actionMapping;
	}
}