/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {

    public static String ACTION_BEAN_NAME_SUFFIX = "Action";

    private Set actionBeanNames;

    private boolean updateAction;

    public ActionState(String id, Transition transition) {
        super(id, transition);
        setBeanName(appendBeanNameSuffix(id));
    }

    public ActionState(String id, Transition[] transitions) {
        super(id, transitions);
        setBeanName(appendBeanNameSuffix(id));
    }

    public ActionState(String id, String beanName, Transition transition) {
        super(id, transition);
        setBeanName(beanName);
    }

    public ActionState(String id, String beanName, Transition[] transitions) {
        super(id, transitions);
        setBeanName(beanName);
    }

    public boolean isActionState() {
        return true;
    }

    public void setBeanName(String beanName) {
        Assert.hasText(beanName, "The action bean name is required");
        this.actionBeanNames = new HashSet(1);
        this.actionBeanNames.add(beanName);
    }

    private String appendBeanNameSuffix(String stateId) {
        return stateId + ACTION_BEAN_NAME_SUFFIX;
    }

    public void setBeanNames(String[] beanNames) {
        this.actionBeanNames = new HashSet(Arrays.asList(beanNames));
    }

    protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        Iterator it = actionBeanNames.iterator();
        while (it.hasNext()) {
            String beanName = (String)it.next();
            ActionBean bean = (ActionBean)flow.getFlowDao().getActionBean(beanName);
            if (logger.isDebugEnabled()) {
                logger.debug("Executing action bean with name '" + beanName + "'");
            }
            ActionBeanEvent event = bean.execute(request, response, sessionExecutionStack);
            if (triggersTransition(event, flow)) {
                return getTransition(event, flow).execute(flow, sessionExecutionStack, request, response);
            }
            else {
                if (event != null && logger.isWarnEnabled()) {
                    logger.warn("Event '" + event + "' returned by action bean " + bean
                            + "' does not map to a valid state transition for action state '" + getId() + "' in flow '"
                            + flow.getId() + "'");
                }
            }
        }
        throw new IllegalStateException(
                "No valid event was signaled by the action bean(s) associated with action state '" + getId()
                        + "' of flow '" + flow.getId() + "' - programmer error?");
    }

    protected boolean triggersTransition(ActionBeanEvent event, Flow flow) {
        return getTransition(event, flow) != null;
    }

    protected Transition getTransition(ActionBeanEvent event, Flow flow) {
        if (event == null) {
            return null;
        }
        return getTransition(event.getId(), flow);
    }

    public boolean isUpdateAction() {
        return updateAction;
    }

    public void setUpdateAction(boolean updateAction) {
        this.updateAction = updateAction;
    }
}