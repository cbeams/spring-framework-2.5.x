/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow.struts;

import org.apache.struts.action.ActionMapping;

/**
 * @author Keith Donald
 */
public class FlowActionMapping extends ActionMapping {
    private String flowId;

    /**
     * @return Returns the flowId.
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * @param flowId The flowId to set.
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
}