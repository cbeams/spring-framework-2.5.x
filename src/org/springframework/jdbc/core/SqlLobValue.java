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

package org.springframework.jdbc.core;

import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Object to represent an SQL CLOB/BLOB value parameter.  CLOBs can be in the
 * form of a Reader, InputStream or String.  BLOBs can either be InputStrem 
 * or a byte array.  Each CLOB/BLOB value will be stored together with
 * its length.  The type is based on which constructor is used.  Objects of this
 * class are imutable except for the LobCreator reference.  Use them and discard 
 * them.
 *
 * This class holds a reference to a LocCreator that must be closed after the 
 * update has completed.  This is done via a call to the closeLobCreator method.  
 * All handling of the LobCreator is done by the framework classes that use it -
 * no need to set or close the LobCreator for end users of this class.
 *   
 * @author Thomas Risberg
 * @since 06.01.2004
 */
public class SqlLobValue {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Public constants used to determine how the Lob value is stored
	 */
	public final static int BYTES = 1;
	public final static int STREAM = 2;
	public final static int STRING = 3;
	public final static int READER = 4;
	
	/**
	 * This contains the type of the Lob data stored in this parameter
	 */
	private int type;

	/**
	 * This contains the length of the Lob stored in this parameter
	 */
	private int length;

	/**
	 * This contains the length of the Lob stored in this parameter
	 */
	private byte[] bytes;
	private InputStream stream;
	private Reader reader;
	private String string;
	
	/**
	 * This contains a reference to the LobCreator - so we can close it
	 * once the update is done.
	 */
	private LobCreator lobCreator;


	/**
	 * @param reader The character stream containing the CLOB value
	 * @param length The length of the CLOB value
	 */
	public SqlLobValue(Reader reader, int length) {
		this.type = READER;
		this.reader = reader;
		this.length = length;
	}

	/**
	 * @param reader The character stream containing the LOB value
	 * @param length The length of the LOB value
	 */
	public SqlLobValue(InputStream stream, int length) {
		this.type = STREAM;
		this.stream = stream;
		this.length = length;
	}

	/**
	 * @param bytes The byte array containing the BLOB value
	 */
	public SqlLobValue(byte[] bytes) {
		this.type = BYTES;
		this.bytes = bytes;
		if (bytes != null)
			this.length = bytes.length;
		else
			this.length = 0;
	}

	/**
	 * @param s The String containing the CLOB value
	 */
	public SqlLobValue(String s) {
		this.type = STRING;
		this.string = s;
		if (s != null)
			this.length = s.length();
		else
			this.length = 0;
			
	}

	/**
	 * @return Returns the Reader for the Clob.
	 */
	public Reader getReader() {
		return reader;
	}

	/**
	 * @return Returns the Stream for the Lob.
	 */
	public InputStream getStream() {
		return stream;
	}

	/**
	 * @return Returns the length of the Lob.
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return Returns the bytes.
	 */
	public byte[] getBytes() {
		return bytes;
	}
	/**
	 * @return Returns the string.
	 */
	public String getString() {
		return string;
	}

	/**
	 * @return Returns a new LobCreator for the Lob.
	 * 
	 * This LobCreator must be held on to so we can close
	 * it later once the update is completed.  That is done by
	 * a call to closeLobCreaator().
	 */
	public LobCreator newLobCreator(LobHandler lh) {
		lobCreator = lh.getLobCreator();
		return lobCreator;
	}

	/**
	 * Close the LobCreator if it was created
	 */
	public void closeLobCreator() {
		if (lobCreator != null) {
			logger.debug("Closing LobCreator");
			lobCreator.close();
		}
		lobCreator = null;
	}

}
