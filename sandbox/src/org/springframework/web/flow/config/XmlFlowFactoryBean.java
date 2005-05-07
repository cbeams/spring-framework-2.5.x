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
 * </table>
 * @author Keith Donald
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
	
	/**
	 * Set the factory used to create transition criteria.
	 */
	public void setTransitionCriteriaCreator(TransitionCriteriaCreator creator) {
		getXmlFlowBuilder().setTransitionCriteriaCreator(creator);
	}

	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.isInstanceOf(XmlFlowBuilder.class, flowBuilder);
		super.setFlowBuilder(flowBuilder);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		getXmlFlowBuilder().setBeanFactory(beanFactory);
	}
	
	/**
	 * @return the XML flow builder
	 */
	protected XmlFlowBuilder getXmlFlowBuilder() {
		return (XmlFlowBuilder)getFlowBuilder();
	}
}