package org.springframework.web.flow.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

/**
 * A client flow event orignating from a struts environment. Provides access to the struts ActionMapping and ActionForm
 * objects, as well as all HttpServlet information.
 * 
 * @author Keith Donald
 */
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
	 * Creates a new struts event.
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
	 * Creates a new struts event.
	 * @param actionMapping the action mapping
	 * @param actionForm the action form
	 * @param request the request
	 * @param response the response
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