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

import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

/**
 * Callback interface used by CciTemplate's query methods.
 * Implementations of this interface perform the actual work of generating
 * records to be pass to CCI. It extends the RecordGenerator in order to
 * allow to work with indexed and mapped CCI records.
 *
 * @author Thierry Templier
 */
public interface RecordGeneratorFromFactory extends RecordGenerator {
	public void setConnectionFactory(ConnectionFactory connectionFactory);
	public IndexedRecord createIndexedRecord(String name);
	public MappedRecord createMappedRecord(String name);
}
