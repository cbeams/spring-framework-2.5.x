/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */

package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to be implemented by objects that can listen to the operation of a
 * Flow object.
 * @author Keith Donald
 */
public interface FlowLifecycleListener {

    public void flowStarted(Flow source, FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request);

    public void flowEventSignaled(Flow source, String eventId, AbstractState state,
            FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request);

    public void flowStateTransitioned(Flow source, AbstractState oldState, AbstractState newState,
            FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request);

    public void flowEventProcessed(Flow source, String eventId, AbstractState state,
            FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request);

    public void flowEnded(Flow source, FlowSession endedFlowSession, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request);

}