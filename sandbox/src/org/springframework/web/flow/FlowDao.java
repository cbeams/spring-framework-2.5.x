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