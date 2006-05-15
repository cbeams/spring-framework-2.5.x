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
package org.springframework.samples.petportal.domain;

import java.io.Serializable;

/**
 * The PetDescription stores a file's contents as an array of bytes.
 * It is used to demonstrate file upload from within a portlet.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetDescription implements Serializable {

	private static final long serialVersionUID = 5626992748524133629L;

	private byte[] file;
	
	public PetDescription() {
		super();
	}
	
	public PetDescription(byte[] file) {
		this();
		setFile(file);
	}
	
	/**
	 * Set the file as a byte array.
	 * @param file
	 */
	public void setFile(byte[] file) {
		this.file = file;
	}
	
	/**
	 * Return the file as a byte array.
	 * @return file
	 */
	public byte[] getFile() {
		return this.file;
	}

}
