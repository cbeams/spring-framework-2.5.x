/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Convenient specialization of FlowFactoryBean that uses a XmlFlowBuilder to
 * build flows from a XML file.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td><i>null</i></td>
 * <td>Specifies the XML file location from which the flow definition is loaded.
 * This is a required property.</td>
 * </tr>
 * <tr>
 * <td>transitionCriteriaCreator</td>
 * <td><i>{@link org.springframework.web.flow.config.SimpleTransitionCriteriaCreator default}</i></td>
 * <td>Set the factory that creates transition criteria.</td>
 * </tr>
 * <tr>
 * <td>viewDescriptorProducerCreator</td>
 * <td><i>{@link org.springframework.web.flow.config.SimpleViewDescriptorCreatorParser default}</i></td>
 * <td>Set the factory that creates view descriptor producers.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.web.flow.config.XmlFlowBuilder
 * @see org.springframework.web.flow.config.FlowFactoryBean
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class XmlFlowFactoryBean extends FlowFactoryBean implements BeanFactoryAware {

	/**
	 * Creates a XML flow factory bean.
	 */
	public XmlFlowFactoryBean() {
		super(new XmlFlowBuilder());
	}
	
	/**
	 * Set the resource from which an XML flow definition will be read.
	 * @param location the resource location
	 */
	public void setLocation(Resource location) {
		getXmlFlowBuilder().setLocation(location);
	}
	
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.isInstanceOf(XmlFlowBuilder.class, flowBuilder);
		super.setFlowBuilder(flowBuilder);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		getXmlFlowBuilder().setBeanFactory(beanFactory);
	}
	
	/**
	 * Returns the XML based flow builder used by this factory bean.
	 */
	protected XmlFlowBuilder getXmlFlowBuilder() {
		return (XmlFlowBuilder)getFlowBuilder();
	}
}