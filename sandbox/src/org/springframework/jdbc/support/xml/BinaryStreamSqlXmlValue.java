/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.jdbc.support.xml;

import org.springframework.dao.DataAccessResourceFailureException;

import java.sql.SQLXML;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.OutputStream;

/**
 * Implementation of the SqlXmlValue interface that will handle XML input in the form of
 * a binary stream.
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see org.springframework.jdbc.support.xml.SqlXmlHandler
 */
public class BinaryStreamSqlXmlValue implements SqlXmlValue {

	private SQLXML xmlObject;

	private XmlBinaryStreamProvider provider;


	public BinaryStreamSqlXmlValue(XmlBinaryStreamProvider provider) {
		this.provider = provider;
	}


	@Override
	public void cleanup() {
		try {
			xmlObject.free();
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not free SQLXML object", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setTypeValue(PreparedStatement ps, int colIndex,
			int sqlType, String typeName) throws SQLException {
		xmlObject = ps.getConnection().createSQLXML();
		OutputStream outputStream = xmlObject.setBinaryStream();
		provider.provideXml(outputStream);
		ps.setSQLXML(colIndex, xmlObject);
	}

}
