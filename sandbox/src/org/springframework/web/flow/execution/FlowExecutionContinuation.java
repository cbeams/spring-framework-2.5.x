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
package org.springframework.web.flow.execution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.springframework.web.flow.FlowExecution;

/**
 * Helper class that aides in handling a flow execution as if
 * it was a continuation.
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionContinuation implements Serializable {
	
	private byte[] data;
	
	/**
	 * Create a new flow execution continuation using given data,
	 * which should be a serialized representation of a 
	 * <code>FlowExecution</code> object.
	 * @param data serialized flow execution data
	 */
	public FlowExecutionContinuation(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Create a new flow execution continuation for given flow execution.
	 * @param flowExecution the flow execution to wrap
	 * @throws FlowExecutionStorageException when the flow execution cannot
	 *         be serialized
	 */
	public FlowExecutionContinuation(FlowExecution flowExecution) throws FlowExecutionStorageException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(flowExecution);
			oos.flush();
			this.data = baos.toByteArray();
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionStorageException(
					"Could not serialize flow execution -- make sure all objects stored in flow scope are serializable!", e);
		}
		catch (IOException e) {
			throw new FlowExecutionStorageException(
					"IOException creating a flow execution continuation -- this should not happen!", e);
		}
	}
	
	/**
	 * Returns a clone of the flow execution wrapped by this object.
	 * @throws FlowExecutionStorageException when the flow execution cannot
	 *         be restored.
	 */
	public FlowExecution getFlowExecution() throws FlowExecutionStorageException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			return (FlowExecution)ois.readObject();
		}
		catch (IOException e) {
			throw new FlowExecutionStorageException(
					"IOException loading the flow execution continuation -- this should not happen!", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionStorageException(
					"ClassNotFoundException loading the flow execution continuation -- this should not happen!", e);
		}
	}

	/**
	 * Returns the binary representation of the flow execution continuation.
	 * This is actually a serialized version of the continuation.
	 * @return the serialized flow execution data
	 */
	public byte[] getData() {
		return data;
	}
}