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
package org.springframework.web.flow.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.Flow;

/**
 * Factory bean that acts as a director for assembling flows, delegating to a
 * <code>FlowBuilder</code> builder to construct the Flow. This is the core
 * top level class for assembling a <code>Flow</code> from top-level
 * configuration information.
 * <p>
 * As an example, a Spring-managed FlowFactoryBean definition might look like
 * this:
 * 
 * <pre>
 * &lt;bean id="user.RegistrationFlow" class="org.springframework.web.flow.config.FlowFactoryBean"&gt;
 *    &lt;property name="flowBuilder"&gt;
 *        &lt;bean class="com.mycompany.myapp.web.flow.user.UserRegistrationFlowBuilder"/&gt;
 *    &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * The above definition is configured with a specific, java-based FlowBuilder
 * implementation. A XmlFlowBuilder could instead be used, for example:
 * 
 * <pre>
 * &ltbean id="user.RegistrationFlow" class="org.springframework.web.flow.config.FlowFactoryBean"&gt;
 *     &ltproperty name="flowBuilder"&gt;
 *         &ltbean class="org.springframework.web.flow.config.XmlFlowBuilder"&gt;
 *             &ltproperty name="resource"&gt;
 *                 &ltvalue&gt;UserRegistrationFlow.xml&lt/value&gt;
 *             &lt/property&gt;
 *         &lt/bean&gt;
 *      &lt/property&gt;
 * &lt/bean&gt;
 * </pre>
 * 
 * </p>
 * Flow factory beans, as POJOs, can also be used outside of a Spring bean
 * factory, in a standalone, programmatic fashion:
 * 
 * <pre>
 * FlowBuilder builder = ...;
 * Flow flow = new FlowFactoryBean(builder).getFlow();
 * </pre>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowFactoryBean implements FactoryBean, InitializingBean {

	private FlowBuilder flowBuilder;

	private Flow flow;

	/**
	 * Create a new flow factory bean.
	 */
	public FlowFactoryBean() {
	}

	/**
	 * Create a new flow factory bean.
	 * @param flowBuilder The builder the factory will use to build flows.
	 */
	public FlowFactoryBean(FlowBuilder flowBuilder) {
		setFlowBuilder(flowBuilder);
	}

	/**
	 * @return The builder the factory uses to build flows.
	 */
	protected FlowBuilder getFlowBuilder() {
		return this.flowBuilder;
	}

	/**
	 * @param flowBuilder The builder the factory will use to build flows.
	 */
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	public void afterPropertiesSet() {
		Assert.state(flowBuilder != null, "The flow builder is required to assemble the flow produced by this factory");
	}

	/**
	 * Does this factory bean build flows with the specified FlowBuilder
	 * implementation?
	 * @param builderImplementationClass The builder implementation
	 * @return true if yes, false otherwise
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
	 * @return The flow built by this factory.
	 */
	public Flow getFlow() {
		if (this.flow == null) {
			new FlowAssembler(this.flowBuilder).assemble();
			this.flow = this.flowBuilder.getResult();
		}
		return this.flow;
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Helper class to direct flow construction using the builder. This object
	 * is the "director" in the GoF builder pattern.
	 */
	private static class FlowAssembler {
		private FlowBuilder builder;

		public FlowAssembler(FlowBuilder builder) {
			this.builder = builder;
		}

		public void assemble() {
			this.builder.init();
			this.builder.buildStates();
			this.builder.buildExecutionListeners();
		}
	}
}