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

package org.springframework.orm.ibatis.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * @author Juergen Hoeller
 * @since 1.2
 */
public class BlobSerializableTypeHandler extends AbstractLobTypeHandler {

	protected void setParameterInternal(
			PreparedStatement ps, int index, Object parameter, String jdbcType, LobCreator lobCreator)
			throws SQLException, IOException {
		if (parameter != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(parameter);
				oos.flush();
				lobCreator.setBlobAsBytes(ps, index, baos.toByteArray());
			}
			finally {
				oos.close();
			}
		}
		else {
			lobCreator.setBlobAsBytes(ps, index, null);
		}
	}

	protected Object getResultInternal(ResultSet rs, int columnIndex, LobHandler lobHandler)
			throws SQLException, IOException {
		InputStream is = lobHandler.getBlobAsBinaryStream(rs, columnIndex);
		if (is != null) {
			ObjectInputStream ois = new ObjectInputStream(is);
			try {
				return ois.readObject();
			}
			catch (ClassNotFoundException ex) {
				throw new SQLException("Could not deserialize BLOB contents: " + ex.getMessage());
			}
			finally {
				ois.close();
			}
		}
		else {
			return null;
		}
	}

	public Object valueOf(String s) {
		return s.getBytes();
	}

}
