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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.Flow;

/**
 * Factory bean that acts as a director for assembling flows, delegating to a
 * <code>FlowBuilder</code> builder to construct the Flow. This is the core
 * top level class for assembling a <code>Flow</code> from configuration
 * information.
 * 
 * <p>
 * As an example, a Spring-managed FlowFactoryBean definition might look like
 * this:
 * 
 * <pre>
 *    &lt;bean id=&quot;user.RegistrationFlow&quot; class=&quot;org.springframework.web.flow.config.FlowFactoryBean&quot;&gt;
 *       &lt;property name=&quot;flowBuilder&quot;&gt;
 *           &lt;bean class=&quot;com.mycompany.myapp.web.flow.user.UserRegistrationFlowBuilder&quot;/&gt;
 *       &lt;/property&gt;
 *    &lt;/bean&gt;
 * </pre>
 * 
 * The above definition is configured with a specific, Java-based FlowBuilder
 * implementation. An XmlFlowBuilder could instead be used, for example:
 * 
 * <pre>
 *    &lt;bean id=&quot;user.RegistrationFlow&quot; class=&quot;org.springframework.web.flow.config.FlowFactoryBean&quot;&gt;
 *        &lt;property name=&quot;flowBuilder&quot;&gt;
 *            &lt;bean class=&quot;org.springframework.web.flow.config.XmlFlowBuilder&quot;&gt;
 *                &lt;property name=&quot;resource&quot;&gt;
 *                    &lt;value&gt;UserRegistrationFlow.xml&lt;/value&gt;
 *                &lt;/property&gt;
 *            &lt;/bean&gt;
 *         &lt;/property&gt;
 *    &lt;/bean&gt;
 * </pre>
 * 
 * </p>
 * Flow factory beans, as POJOs, can also be used outside of a Spring bean
 * factory, in a standalone, programmatic fashion:
 * 
 * <pre>
 *    FlowBuilder builder = ...;
 *    Flow flow = new FlowFactoryBean(builder).getFlow();
 * </pre>
 * 
 * <p>
 * <b>Exposed configuration properties:</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowBuilder</td>
 * <td><i>null</i></td>
 * <td>Set the builder the factory will use to build flows. This is a required
 * property.</td>
 * </tr>
 * </table>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * The flow builder strategy used to assemble the flow produced by this
	 * factory bean.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The flow assembled by this factory bean.
	 */
	private Flow flow;

	/**
	 * Create a new flow factory bean.
	 */
	public FlowFactoryBean() {
	}

	/**
	 * Create a new flow factory bean using the specified builder strategy.
	 * @param flowBuilder the builder the factory will use to build flows.
	 */
	public FlowFactoryBean(FlowBuilder flowBuilder) {
		setFlowBuilder(flowBuilder);
	}

	/**
	 * Returns the builder the factory uses to build flows.
	 */
	protected FlowBuilder getFlowBuilder() {
		return this.flowBuilder;
	}

	/**
	 * Set the builder the factory will use to build flows.
	 */
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	public void afterPropertiesSet() {
		Assert.notNull(flowBuilder, "The flow builder is required to assemble the flow produced by this factory");
	}

	/**
	 * Does this factory bean build flows with the specified FlowBuilder
	 * implementation?
	 * @param builderImplementationClass the builder implementation
	 * @return true if yes, false otherwise
	 * @throws IllegalArgumentException if specified class is not a
	 *         <code>FlowBuilder</code> implementation
	 */
	public boolean buildsWith(Class builderImplementationClass) throws IllegalArgumentException {
		if (builderImplementationClass == null) {
			return false;
		}
		if (!FlowBuilder.class.isAssignableFrom(builderImplementationClass)) {
			throw new IllegalArgumentException("The flow builder implementation class '" + builderImplementationClass
					+ "' you provided to this method does not implement the '" + FlowBuilder.class.getName()
					+ "' interface");
		}
		return getFlowBuilder().getClass().equals(builderImplementationClass);
	}

	public Object getObject() throws Exception {
		return getFlow();
	}

	/**
	 * Returns the flow built by this factory.
	 */
	public synchronized Flow getFlow() {
		if (this.flow == null) {
			// already set the flow handle to avoid infinite loops!
			// e.g where Flow A spawns Flow B, which spawns Flow A again...
			this.flow = this.flowBuilder.init();
			this.flowBuilder.buildStates();
			this.flow = this.flowBuilder.getResult();
			this.flowBuilder.dispose();
		}
		return this.flow;
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return true;
	}
}