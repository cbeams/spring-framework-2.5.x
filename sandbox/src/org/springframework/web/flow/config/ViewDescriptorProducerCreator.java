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

import org.springframework.web.flow.ViewDescriptorProducer;

/**
 * Interface for factories that create view descriptor producers.
 * Note that this is a configuration time factory for factory objects
 * (the producers) that are used a runtime during flow execution.
 * So this interface basically defines a factory for view descriptor factories.
 * 
 * @see org.springframework.web.flow.ViewDescriptor
 * @see org.springframework.web.flow.ViewDescriptorProducer
 * 
 * @author Erwin Vervaet
 */
public interface ViewDescriptorProducerCreator {
	
	/**
	 * Create a new view descriptor producer based on the information
	 * in given encoded view name.
	 * @param encodedView the encoded view name
	 * @return the view descriptor factory
	 */
	public ViewDescriptorProducer create(String encodedView);

}
