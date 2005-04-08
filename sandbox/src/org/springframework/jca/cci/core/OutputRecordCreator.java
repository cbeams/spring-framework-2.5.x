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

import javax.resource.cci.Record;

/**
 * Class used to create output record automatically when
 * boolean execute(InteractionSpec,Record,Record) is used
 * and no output record is passed to cci template or query. 
 * 
 * @author Thierry TEMPLIER
 */
public interface OutputRecordCreator {
	/**
	 * method used to create specify a default creation of
	 * output record. Commonly, it could be used when the
	 * connector used only supports the boolean execute(InteractionSpec,Record,Record). 
	 * @return the output record
	 */
	public Record createOutputRecord();
}
