package org.springframework.web.flow.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.config.FlowConstants;
import org.springframework.web.flow.config.FlowServiceLocator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.WebUtils;

/**
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	private Flow flow;

	private FlowServiceLocator flowServiceLocator;

	private Collection flowExecutionListeners = new ArrayList(3);

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.add(listener);
	}

	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.addAll(Arrays.asList(listeners));
	}

	protected String getFlowIdParameterName() {
		return FlowConstants.FLOW_ID_PARAMETER;
	}

	protected String getFlowExecutionIdParameterName() {
		return FlowConstants.FLOW_EXECUTION_ID_PARAMETER;
	}

	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	private String getEventIdAttributeName() {
		return FlowConstants.EVENT_ID_ATTRIBUTE;
	}

	protected String getNotSetEventIdParameterMarker() {
		return FlowConstants.NOT_SET_EVENT_ID;
	}

	protected String getFlowExecutionIdAttributeName() {
		return FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE;
	}

	protected String getCurrentStateIdAttributeName() {
		return FlowConstants.CURRENT_STATE_ID_ATTRIBUTE;
	}

	protected String getFlowExecutionInfoAttributeName() {
		return FlowExecution.ATTRIBUTE_NAME;
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		FlowExecution flowExecution;
		ModelAndView modelAndView;
		if (isNewFlowExecutionRequest(request)) {
			// start a new flow execution
			if (flow == null) {
				// try to extract flow definition to use from request
				Assert.notNull(flowServiceLocator, "The flow service locator is required");
				flow = flowServiceLocator.getFlow(RequestUtils.getRequiredStringParameter(request,
						getFlowIdParameterName()));
			}
			flowExecution = createFlowExecution(flow);
			modelAndView = flowExecution.start(getFlowExecutionInput(request), request, response);
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
			modelAndView = flowExecution.signalEvent(eventId, stateId, request, response);
		}

		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		else {
			// We're still in the flow, inject flow model into request
			if (modelAndView != null) {
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
			logger.debug("Returning selected model and view " + modelAndView);
		}
		return modelAndView;
	}

	protected boolean isNewFlowExecutionRequest(HttpServletRequest request) {
		return RequestUtils.getStringParameter(request, getFlowExecutionIdParameterName(), null) == null;
	}

	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = flow.createFlowExecution();
		if (!flowExecutionListeners.isEmpty()) {
			flowExecution.getListenerList().add(
					(FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[0]));
		}
		return flowExecution;
	}

	protected Map getFlowExecutionInput(HttpServletRequest request) {
		return null;
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