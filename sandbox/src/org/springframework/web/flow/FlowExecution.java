/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Subinterface of <code>FlowSessionExecutionInfo</code> that exposes mutable
 * operations. Designed for use by the <code>FlowSessionExecutionListener</code>,
 * which is a bit more privleged than pure for-management clients. This
 * interface may also be used in situations where other privileged objects need
 * access to flow definition configuration details during session execution.
 * 
 * Note: though these definitions are exposed to clients via this interface,
 * they should *not* be modified post application startup. Mutable operations on
 * mutable configuration operations should be treated as frozen after a flow's
 * session execution commences.
 * 
 * @author Keith Donald
 */
public interface FlowExecution extends FlowExecutionInfo, MutableAttributesAccessor {

	/**
	 * Returns this session execution's active flow definition.
	 * @return The active flow definition
	 */
	public Flow getActiveFlow();

	/**
	 * Returns this session execution's root flow definition.
	 * @return The root flow definition.
	 */
	public Flow getRootFlow();

	/**
	 * Returns this session execution's current state definition.
	 * @return the current state definition.
	 */
	public AbstractState getCurrentState();

	/**
	 * @param input
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView start(Map input, HttpServletRequest request, HttpServletResponse response);

	/**
	 * @param eventId
	 * @param currentStateId
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView signalEvent(String eventId, String currentStateId, HttpServletRequest request,
			HttpServletResponse response);

	/**
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView spawn(Flow flow, Map input, HttpServletRequest request, HttpServletResponse response);

}