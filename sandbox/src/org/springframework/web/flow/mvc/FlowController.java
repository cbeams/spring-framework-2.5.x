package org.springframework.web.flow.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.RequestUtils;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowExecutionStack;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.struts.FlowAction;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.WebUtils;

/**
 * @author Erwin Vervaet
 */
public class FlowController extends AbstractController {

	private Flow flow;

	private FlowExecutionListener[] flowExecutionListeners;

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	protected String getFlowExecutionIdParameterName() {
		return FlowAction.FLOW_EXECUTION_ID_PARAMETER;
	}

	protected String getCurrentStateIdParameterName() {
		return FlowAction.CURRENT_STATE_ID_PARAMETER;
	}

	protected String getEventIdParameterName() {
		return FlowAction.EVENT_ID_PARAMETER;
	}

	private String getEventIdAttributeName() {
		return FlowAction.EVENT_ID_ATTRIBUTE;
	}

	protected String getNotSetEventIdParameterMarker() {
		return FlowAction.NOT_SET_EVENT_ID;
	}

	protected String getFlowExecutionIdAttributeName() {
		return FlowAction.FLOW_EXECUTION_ID_ATTRIBUTE;
	}

	protected String getCurrentStateIdAttributeName() {
		return FlowAction.CURRENT_STATE_ID_ATTRIBUTE;
	}

	protected String getFlowExecutionInfoAttributeName() {
		return FlowExecution.ATTRIBUTE_NAME;
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		FlowExecution flowExecution;
		ModelAndView mv;
		if (RequestUtils.getStringParameter(request, getFlowExecutionIdParameterName(), null) == null) {
			// No existing flow execution to lookup as no _flowExecutionId
			// was provided - start a new one
			flowExecution = createFlowExecution(flow);
			mv = flowExecution.start(null, request, response);
			saveInHttpSession(flowExecution, request);
		}
		else {
			// Client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(RequestUtils.getRequiredStringParameter(request,
					getFlowExecutionIdParameterName()), request);

			// let client tell you what state they are in (if possible)
			String stateId = RequestUtils.getStringParameter(request, getCurrentStateIdParameterName(), null);

			// let client tell you what event was signaled in the current state
			String eventId = RequestUtils.getStringParameter(request, getEventIdParameterName(), null);

			if (eventId == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("No '" + getEventIdParameterName()
							+ "' parameter was found; falling back to request attribute");
				}
				eventId = (String)request.getAttribute(getEventIdAttributeName());
			}
			if (eventId == null) {
				throw new IllegalArgumentException(
						"The '"
								+ getEventIdParameterName()
								+ "' request parameter (or '"
								+ getEventIdAttributeName()
								+ "' request attribute) is required to signal an event in the current state of this executing flow '"
								+ flowExecution.getCaption() + "' -- programmer error?");
			}
			if (eventId.equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId submitted by the browser was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' - this is likely a view (jsp, etc) configuration error - " + "the '"
						+ getEventIdParameterName()
						+ "' parameter must be set to a valid event to execute within the current state '" + stateId
						+ "' of this flow '" + flowExecution.getCaption() + "' - else I don't know what to do!");
			}

			// execute the signaled event within the current state
			mv = flowExecution.signalEvent(eventId, stateId, request, response);
		}

		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		else {
			// We're still in the flow, inject flow model into request
			if (mv != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[Placing information about the new current flow state in request scope]");
					logger.debug("    - " + getFlowExecutionIdAttributeName() + "=" + flowExecution.getId());
					logger.debug("    - " + getCurrentStateIdAttributeName() + "=" + flowExecution.getCurrentStateId());
				}
				request.setAttribute(getFlowExecutionIdAttributeName(), flowExecution.getId());
				request.setAttribute(getCurrentStateIdAttributeName(), flowExecution.getCurrentStateId());
				request.setAttribute(getFlowExecutionInfoAttributeName(), flowExecution);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected model and view " + mv);
		}
		return mv;
	}

	public void setFlowExecutionListeners(FlowExecutionListener[] flowExecutionListeners) {
		this.flowExecutionListeners = flowExecutionListeners;
	}

	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionStack(flow);
		flowExecution.addFlowExecutionListeners(flowExecutionListeners);
		return flowExecution;
	}

	protected FlowExecution getRequiredFlowExecution(String flowExecutionId, HttpServletRequest request)
			throws NoSuchFlowExecutionException {
		try {
			return (FlowExecution)WebUtils.getRequiredSessionAttribute(request, flowExecutionId);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(flowExecutionId, e);
		}
	}

	protected void saveInHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution '" + flowExecution.getId() + "' in HTTP session");
		}
		request.getSession().setAttribute(flowExecution.getId(), flowExecution);
	}

	private void removeFromHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution '" + flowExecution.getId() + "' from HTTP session");
		}
		request.getSession().removeAttribute(flowExecution.getId());
	}
}