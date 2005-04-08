
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

package org.springframework.jca.cci.core;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.InteractionSpec;

/** 
 * Default implementation of the CciSpecsHolder interface.
 *
 * @author thierry TEMPLIER
 */
public class DefaultCciSpecsHolder implements CciSpecsHolder {
	protected ConnectionSpec connectionSpec;
	protected InteractionSpec interactionSpec;

	/**
	 * Contructor used to connection and interaction specifications
	 * in the holder. 
	 * @param connectionSpec connection specification 
	 * @param interactionSpec intercation specification
	 */
	public DefaultCciSpecsHolder(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) {
		this.connectionSpec=connectionSpec;
		this.interactionSpec=interactionSpec;
	}

	/**
	 * @see org.springframework.jca.cci.core.CciSpecsHolder#initSpecs(javax.resource.cci.ConnectionSpec, javax.resource.cci.InteractionSpec)
	 */
	public void initSpecs(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) throws ResourceException {
		
	}

	/**
	 * @see org.springframework.jca.cci.core.CciSpecsHolder#getConnectionSpec()
	 */
	public ConnectionSpec getConnectionSpec() {
		return connectionSpec;
	}

	/**
	 * @see org.springframework.jca.cci.core.CciSpecsHolder#getInteractionSpec()
	 */
	public InteractionSpec getInteractionSpec() {
		return interactionSpec;
	}

	/**
	 * @param spec
	 */
	public void setConnectionSpec(ConnectionSpec spec) {
		connectionSpec = spec;
	}

	/**
	 * @param spec
	 */
	public void setInteractionSpec(InteractionSpec spec) {
		interactionSpec = spec;
	}

}
