/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.closure.support.Block;

/**
 * Implementation of FlowLifecycleListener that actually multicasts events to
 * all FlowLifecycleListener implementations in the current Spring IoC context.
 * Broadcasts events in the current thread of execution, as listeners are not
 * meant to take a long time to run.
 * 
 * @author Rod Johnson
 */
public class MulticastFlowLifecycleListener implements FlowLifecycleListener, BeanFactoryAware {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * List of all FlowLifecycleListener found in the current factory
     */
    private List listeners;

    /**
     * 
     * @return the listeners registered in the current Spring IoC context.
     */
    public List getListeners() {
        return listeners;
    }

    /*
     * Save list of FlowLifecycleListeners registered in the current factory.
     * Only consider Singleton bean definitions.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ListableBeanFactory)) {
            throw new IllegalStateException("Cannot use MulticastFlowLifecycleListener except in a ListableBeanFactory");
        }
        ListableBeanFactory lbf = (ListableBeanFactory)beanFactory;
        Map m = lbf.getBeansOfType(FlowLifecycleListener.class, false, true);
        listeners = new ArrayList(m.values());
        // This class implements FlowLifecycleListener, so we must remove it to
        // avoid
        // a stack overflow
        listeners.remove(this);
    }

    /*
     * @see uk.co.voca.common.web.flow.FlowLifecycleListener#flowStarted(uk.co.voca.common.web.flow.Flow,
     *      uk.co.voca.common.web.flow.FlowSessionExecutionStack,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void flowStarted(final Flow source, final FlowSessionExecutionStack sessionExecutionStack,
            final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flow [started] call back received; flowId='" + source.getId() + "', sessionExecutionStack='"
                    + sessionExecutionStack + "'");
        }
        new Block() {
            protected void handle(Object l) {
                ((FlowLifecycleListener)l).flowStarted(source, sessionExecutionStack, request);
            }
        }.forEach(listeners);
    }

    /*
     * @see uk.co.voca.common.web.flow.FlowLifecycleListener#flowEventSignaled(uk.co.voca.common.web.flow.Flow,
     *      java.lang.String, uk.co.voca.common.web.flow.AbstractState,
     *      uk.co.voca.common.web.flow.FlowSessionExecutionStack,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void flowEventSignaled(final Flow source, final String eventId, final AbstractState state,
            final FlowSessionExecutionStack sessionExecutionStack, final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flow [event signaled] call back received; flowId='" + source.getId() + ", eventId='"
                    + eventId + "', stateId='" + state.getId() + "', sessionExecutionStack='" + sessionExecutionStack
                    + "'");
        }
        new Block() {
            protected void handle(Object l) {
                ((FlowLifecycleListener)l).flowEventSignaled(source, eventId, state, sessionExecutionStack, request);
            }
        }.forEach(listeners);
    }

    /*
     * @see uk.co.voca.common.web.flow.FlowLifecycleListener#flowStateTransitioned(uk.co.voca.common.web.flow.Flow,
     *      uk.co.voca.common.web.flow.AbstractState,
     *      uk.co.voca.common.web.flow.AbstractState,
     *      uk.co.voca.common.web.flow.FlowSessionExecutionStack,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void flowStateTransitioned(final Flow source, final AbstractState oldState, final AbstractState newState,
            final FlowSessionExecutionStack sessionExecutionStack, final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flow [state transition] call back received; flowId='" + source.getId() + "', oldStateId='"
                    + (oldState != null ? oldState.getId() : "[none - this is the start state]") + "', newStateId='"
                    + newState.getId() + "', sessionExecutionStack='" + sessionExecutionStack + "'");
        }
        new Block() {
            protected void handle(Object l) {
                ((FlowLifecycleListener)l).flowStateTransitioned(source, oldState, newState, sessionExecutionStack,
                        request);
            }
        }.forEach(listeners);
    }

    /*
     * @see uk.co.voca.common.web.flow.FlowLifecycleListener#flowEventProcessed(uk.co.voca.common.web.flow.Flow,
     *      java.lang.String, uk.co.voca.common.web.flow.AbstractState,
     *      uk.co.voca.common.web.flow.FlowSessionExecutionStack,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void flowEventProcessed(final Flow source, final String eventId, final AbstractState state,
            final FlowSessionExecutionStack sessionExecutionStack, final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flow [event processed] call back received; flowId='" + source.getId() + "', eventId='"
                    + eventId + "', stateId='" + (state != null ? state.getId() : null) + "', sessionExecutionStack='"
                    + sessionExecutionStack + "'");
        }
        new Block() {
            protected void handle(Object l) {
                ((FlowLifecycleListener)l).flowEventProcessed(source, eventId, state, sessionExecutionStack, request);
            }
        }.forEach(listeners);
    }

    /*
     * @see uk.co.voca.common.web.flow.FlowLifecycleListener#flowEnded(uk.co.voca.common.web.flow.Flow,
     *      uk.co.voca.common.web.flow.FlowSessionExecutionStack,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void flowEnded(final Flow source, final FlowSession flowSession,
            final FlowSessionExecutionStack sessionExecutionStack, final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flow [ended] call back received; flowId='" + source.getId() + "', sessionExecutionStack='"
                    + sessionExecutionStack + "'");
        }
        new Block() {
            protected void handle(Object l) {
                ((FlowLifecycleListener)l).flowEnded(source, flowSession, sessionExecutionStack, request);
            }
        }.forEach(listeners);
    }

}