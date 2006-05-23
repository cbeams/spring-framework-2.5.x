/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * Describes the logical view of a set of {@link BeanDefinition BeanDefinitions} and
 * {@link RuntimeBeanReference RuntimeBeanReferences} as presented in some configuration
 * context.
 * <p/>
 * With the introduction of {@link org.springframework.beans.factory.xml.NamespaceHandler pluggable custom XML tags},
 * it is now possible for a single logical configuration entity, in this case an XML tag, to
 * create multiple {@link BeanDefinition BeanDefinitions} and {@link RuntimeBeanReference RuntimeBeanReferences}
 * in order to provide more succinct configuration and greater convenience to end users. As such, it can
 * no longer be assumed that each configuration entity (e.g. XML tag) maps to one {@link BeanDefinition}.
 * For tool vendors and other users who wish to present visualization or support for configuring Spring
 * applications it is important that there is some mechanism in place to tie the {@link BeanDefinition BeanDefinitions}
 * in the {@link BeanFactory} back to the configuration data in a way that has concrete meaning to the end user.
 * As such, {@link org.springframework.beans.factory.xml.NamespaceHandler} implementations are able to publish
 * events in the form of a <code>ComponentDefinition</code> for each logical entity being configured. Third parties
 * can then {@link ReaderEventListener subscribe to these events} allowing for a user-centric view of the
 * bean metadata.
 * <p/>
 * Each <code>ComponentDefinition</code> has a {@link #getSource source object} which is configuration source specific.
 * In the case of XML-based configuration this is typically the {@link org.w3c.dom.Node} which contains the user
 * supplied configuration information. In addition to this, each {@link BeanDefinition} enclosed in a
 * <code>ComponentDefinition</code> has its own {@link BeanDefinition#getSource() source object} which may point
 * to a different, more specific, set of configuration data. Beyond this, individual pieces of bean metadata such
 * as the {@link org.springframework.beans.PropertyValue PropertyValues} may also have a source object giving an
 * even greater level of detail. Source object extraction is handled through the {@link SourceExtractor} which
 * can be customized as required.
 * <p/>
 * Whilst direct access to important {@link RuntimeBeanReference RuntimeBeanReferences} is provided through
 * {@link #getBeanReferences}, tools may wish to inspect all {@link BeanDefinition BeanDefinitions} to gather
 * the full set of {@link RuntimeBeanReference RuntimeBeanReferences}. Implementations are required to provide
 * all {@link RuntimeBeanReference RuntimeBeanReferences} that are required to validate the configuration of the
 * overall logical entity as well as those required to provide full user visualisation of the configuration.
 * It is expected that certain {@link RuntimeBeanReference RuntimeBeanReferences} will not be important to
 * validation or to the user view of the configuration and as such these may be ommitted. A tool may wish to
 * display any additional {@link RuntimeBeanReference RuntimeBeanReferences} sourced through the supplied
 * {@link BeanDefinition BeanDefinitions} but this is not considered to be a typical case.
 * <p/>
 * Tools can determine the important of contained {@link BeanDefinition BeanDefinitions} by checking the
 * {@link BeanDefinition#getRole role identifier}. The role is essentially a hint to the tool as to how
 * important the configuration provider believes a {@link BeanDefinition} is to the end user. It is expected
 * that tools will <strong>not</strong> display all {@link BeanDefinition BeanDefinitions} for a given
 * <code>ComponentDefinition</code> choosing instead to filter based on the role. Tools may choose to make
 * this filtering user configurable. Particular notice should be given to the
 * {@link BeanDefinition#ROLE_INFRASTRUCTURE INFRASTRUCTURE role identifier}. {@link BeanDefinition BeanDefinitions}
 * classified with this role are completely unimportant to the end user and are required only for
 * internal implementation reasons.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public interface ComponentDefinition {

	/**
	 * Gets the user visible name of this <code>ComponentDefinition</code>. This should link back
	 * directly to the corresponding configuration data for this component in a given context.
	 */
	String getName();

	/**
	 * Returns a friendly description of the described component. Implementations are encouraged to
	 * return the same value for {@link #toString()}.
	 */
	String getDescription();

	/**
	 * Returns the {@link BeanDefinition BeanDefinitions} that were registed with the {@link BeanDefinitionRegistry}
	 * to form this <code>ComponentDefinition</code>. It should be noted that a <code>ComponentDefinition</code> may
	 * well be related with other {@link BeanDefinition BeanDefinitions} via {@link RuntimeBeanReference references},
	 * however these are <strong>not</strong> included as they may be not available immediately. Important
	 * {@link RuntimeBeanReference RuntimeBeanReferences} are available from {@link #getBeanReferences()}.
	 */
	BeanDefinition[] getBeanDefinitions();

	/**
	 * Returns the set of {@link RuntimeBeanReference RuntimeBeanReferences} that are considered to be important
	 * to this <code>ComponentDefinition</code>. Other {@link RuntimeBeanReference RuntimeBeanReferences} may
	 * exist on the associated {@link BeanDefinition BeanDefinitions}, however these are not considered to
	 * be needed for validation or for user visualization.
	 */
	RuntimeBeanReference[] getBeanReferences();

	/**
	 * Retreives the <code>Object</code> representing the original configuration data that resulted in
	 * this particular <code>ComponentDefinition</code>. May be <code>null</code>.
	 * @see SourceExtractor
	 */
	Object getSource();
}
