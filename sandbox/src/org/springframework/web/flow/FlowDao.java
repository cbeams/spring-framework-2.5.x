/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import org.springframework.beans.BeansException;

/**
 * DAO interface used by flows to retrieve needed artifacts
 * <p>
 * Typically implemented via Spring's ServiceLocatorProxyCreator, to get
 * artifacts as beans out of a Spring context.
 * </p>
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public interface FlowDao {

    /**
     * Returns the specified ActionBean by Id
     * 
     * @param actionBeanId the action bean id
     * @return The action bean
     */
    public ActionBean getActionBean(String actionBeanId) throws BeansException;

    /**
     * Returns the specified Flow by Id
     * 
     * @param flowId the flow id
     * @return The mapper
     */
    public Flow getFlow(String flowId) throws BeansException;

    /**
     * Returns the Sub Flow attributes mapper by Id
     * 
     * @param subFlowAttributesMapperId the attributes mapper id
     * @return The mapper
     */
    public SubFlowAttributesMapper getSubFlowAttributesMapper(String subFlowAttributesMapperId) throws BeansException;
}