
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
 * Callback interface used by CciTemplate's query methods.
 * Implementations of this interface perform the actual work of extracting
 * results, but don't need to worry about exception handling. ResourceExceptions
 * will be caught and handled correctly by the CciTemplate class.
 *
 * @author thierry TEMPLIER
 */
public interface CciSpecsHolder {
	
	/** 
	 * Implementations must implement this method to process
	 * initializations on CCI connection and interaction.
	 * @param connectionSpec connection specification 
	 * @param interactionSpec intercation specification
	 * @throws ResourceException if a ResourceException is encountered
	 */
	public void initSpecs(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) throws ResourceException;

	/**
	 * Getter on ConnectionSpec 
	 * @return connection specification
	 */
	public ConnectionSpec getConnectionSpec();

	/**
	 * Getter on InteractionSpec
	 * @return interaction specification
	 */
	public InteractionSpec getInteractionSpec();
}
