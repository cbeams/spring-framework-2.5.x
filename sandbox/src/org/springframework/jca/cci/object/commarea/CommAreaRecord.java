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

package org.springframework.jca.cci.object.commarea;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.Streamable;

/**
 * Class for using record based on a commarea.
 * 
 * @author Thierry TEMPLIER
 */
public class CommAreaRecord implements Record, Streamable {
	private byte[] bytes;
	private String recordName;
	private String recordShortDescription;

	public CommAreaRecord() {
	}

	public CommAreaRecord(byte[] bytes) {
		this.bytes=bytes;
	}

	/**
	 * @see javax.resource.cci.Record#getRecordName()
	 */
	public String getRecordName() {
		return recordName;
	}

	/**
	 * @see javax.resource.cci.Record#setRecordName(java.lang.String)
	 */
	public void setRecordName(String recordName) {
		this.recordName=recordName;
	}

	/**
	 * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
	 */
	public void setRecordShortDescription(String recordShortDescription) {
		this.recordShortDescription=recordShortDescription;
	}

	/**
	 * @see javax.resource.cci.Record#getRecordShortDescription()
	 */
	public String getRecordShortDescription() {
		return recordShortDescription;
	}

	/**
	 * @see javax.resource.cci.Streamable#read(java.io.InputStream)
	 */
	public void read(InputStream in) throws IOException {
		bytes=new byte[in.available()];
		in.read(bytes);
	}

	/**
	 * @see javax.resource.cci.Streamable#write(java.io.OutputStream)
	 */
	public void write(OutputStream out) throws IOException {
		out.write(bytes);
		out.flush();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * @param bs
	 */
	public void setBytes(byte[] bs) {
		bytes = bs;
	}

}
