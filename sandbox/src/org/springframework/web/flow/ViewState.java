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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ToStringCreator;

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for solicting form input.
 * 
 * @author Keith Donald
 */
public class ViewState extends TransitionableState {

    protected static final String VIEW_SUFFIX = "View";

    /**
     * The logical name of the view.
     */
    private String viewName;

    public ViewState(String id, Transition transition) {
        this(id, new Transition[] { transition });
    }

    public ViewState(String id, Transition[] transitions) {
        this(id, id, transitions);
    }

    public ViewState(String id, String viewName) {
        super(id);
        setViewName(viewName);
    }

    public ViewState(String id, String viewName, Transition transition) {
        super(id);
        setViewName(viewName);
        add(transition);
    }

    public ViewState(String id, String viewName, Transition[] transitions) {
        super(id);
        setViewName(viewName);
        addAll(transitions);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public boolean isViewState() {
        return true;
    }

    /**
     * Return a view descriptor pointing requesting front controllers to a
     * logical view resource to be displayed. The descriptor also contains a
     * model map needed when the view is rendered, for populating dynamic
     * content.
     * 
     * @param flow The flow definition associated with the executing flow
     *        session
     * @param sessionExecutionStack The session execution stack, tracking the
     *        current active flow session
     * @param request The client http request
     * @param response The server http response
     * @return A view descriptor containing model and view information needed to
     *         render the results of the event execution.
     */
    protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        return new ViewDescriptor(viewName, sessionExecutionStack.getAttributes());
    }

    public String toString() {
        return super.toString() + new ToStringCreator(this).append("viewName", viewName).toString();
    }

}