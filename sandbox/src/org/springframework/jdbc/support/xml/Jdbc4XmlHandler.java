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

import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * Implementation of the SqlXmlHandler interface.  Provides database specific
 * implementations for handling storing and retrieving XML documents to and from
 * fields in a database.
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see org.springframework.jdbc.support.xml.SqlXmlHandler
 */
public class Jdbc4XmlHandler implements SqlXmlHandler {

    public String getXmlAsString(ResultSet rs, String columnName) throws SQLException {
        SQLXML oxml = rs.getSQLXML(columnName);
        String value = oxml.getString();
        oxml.free();
        return value;
    }

    public String getXmlAsString(ResultSet rs, int columnIndex) throws SQLException {
    	SQLXML oxml = rs.getSQLXML(columnIndex);
        String value = oxml.getString();
        oxml.free();
        return value;
    }

    public InputStream getXmlAsBinaryStream(final ResultSet rs, final String columnName) throws SQLException {
        return doGetXmlAsBinaryStream(
                new XmlTypeProvider() {
                    public SQLXML getXmlType() throws SQLException {
                        return rs.getSQLXML(columnName);
                    }
                });
    }

    public InputStream getXmlAsBinaryStream(final ResultSet rs, final int columnIndex) throws SQLException {
        return doGetXmlAsBinaryStream(
                new XmlTypeProvider() {
                    public SQLXML getXmlType() throws SQLException {
                        return rs.getSQLXML(columnIndex);
                    }
                });
    }

    private InputStream doGetXmlAsBinaryStream(XmlTypeProvider provider) throws SQLException {
    	SQLXML oxml = provider.getXmlType();
        InputStream is = oxml.getBinaryStream();
        oxml.free();
        return is;
    }

    public Reader getXmlAsCharacterStream(ResultSet rs, String columnName) throws SQLException {
        throw new SqlXmlFeatureNotImplementedException("getXmlAsCharacterStream method is not implemented yet");
    }

    public Reader getXmlAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException {
        throw new SqlXmlFeatureNotImplementedException("getXmlAsCharacterStream method is not implemented yet");
    }

    @SuppressWarnings("unchecked")
	public Source getXmlAsSource(final ResultSet rs, final String columnName, Class sourceClass) throws SQLException {
        return doGetXmlAsSource(
                new XmlTypeProvider() {
                    public SQLXML getXmlType() throws SQLException {
                        return rs.getSQLXML(columnName);
                    }
                },
                sourceClass);
    }

    @SuppressWarnings("unchecked")
	public Source getXmlAsSource(final ResultSet rs, final int columnIndex, Class sourceClass) throws SQLException {
        return doGetXmlAsSource(
                new XmlTypeProvider() {
                    public SQLXML getXmlType() throws SQLException {
                        return rs.getSQLXML(columnIndex);
                    }
                }, 
                sourceClass);
    }

    @SuppressWarnings("unchecked")
	private Source doGetXmlAsSource(XmlTypeProvider provider, Class sourceClass) throws SQLException {
        Class sourceClassToUse;
        if (sourceClass == null) {
    		sourceClassToUse = DOMSource.class;
        }
        else {
            sourceClassToUse = sourceClass;
        }
        Source source;
        SQLXML oxml = provider.getXmlType();
        source = oxml.getSource(sourceClassToUse);
        return source;
    }

    public SqlXmlValue newSqlXmlValue(String value) {
        return new StringSqlXmlValue(value);
    }

    public SqlXmlValue newSqlXmlValue(XmlBinaryStreamProvider provider) {
		return new BinaryStreamSqlXmlValue(provider);
    }

    public SqlXmlValue newSqlXmlValue(XmlCharacterStreamProvider provider) {
		return new CharacterStreamSqlXmlValue(provider);
    }

	public SqlXmlValue newSqlXmlValue(Class resultClass, XmlResultProvider provider) {
        return new ResultSqlXmlValue(resultClass, provider);
	}

	public SqlXmlValue newSqlXmlValue(Document document) {
		return new DocumentSqlXmlValue(document);
    }

    private interface XmlTypeProvider {
        public abstract SQLXML getXmlType() throws SQLException;
    }

}
