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

package org.springframework.orm.hibernate.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 * @since 14.08.2004
 */
public class LobTypeTests extends TestCase {

	public void testClobStringType() throws Exception {
		MockControl rsControl = MockControl.createControl(ResultSet.class);
		ResultSet rs = (ResultSet) rsControl.getMock();
		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		MockControl lobHandlerControl = MockControl.createControl(LobHandler.class);
		LobHandler lobHandler = (LobHandler) lobHandlerControl.getMock();
		MockControl lobCreatorControl = MockControl.createControl(LobCreator.class);
		LobCreator lobCreator = (LobCreator) lobCreatorControl.getMock();

		rs.findColumn("column");
		rsControl.setReturnValue(1);
		lobHandler.getClobAsString(rs, 1);
		lobHandlerControl.setReturnValue("content");
		lobHandler.getLobCreator();
		lobHandlerControl.setReturnValue(lobCreator);
		lobCreator.setClobAsString(ps, 1, "content");
		lobCreatorControl.setVoidCallable(1);
		lobCreator.close();
		lobCreatorControl.setVoidCallable(1);

		rsControl.replay();
		psControl.replay();
		lobHandlerControl.replay();
		lobCreatorControl.replay();

		ClobStringType type = new ClobStringType(lobHandler);
		assertEquals(1, type.sqlTypes().length);
		assertEquals(Types.CLOB, type.sqlTypes()[0]);
		assertEquals(String.class, type.returnedClass());
		assertTrue(type.equals("content", "content"));
		assertEquals("content", type.deepCopy("content"));
		assertFalse(type.isMutable());

		assertEquals("content", type.nullSafeGet(rs, new String[] {"column"}, null));
		TransactionSynchronizationManager.initSynchronization();
		try {
			type.nullSafeSet(ps, "content", 1);
			List synchs = TransactionSynchronizationManager.getSynchronizations();
			assertEquals(1, synchs.size());
			((TransactionSynchronization) synchs.get(0)).beforeCompletion();
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}

		rsControl.verify();
		psControl.verify();
		lobHandlerControl.verify();
		lobCreatorControl.verify();
	}

	public void testBlobByteArrayType() throws Exception {
		MockControl rsControl = MockControl.createControl(ResultSet.class);
		ResultSet rs = (ResultSet) rsControl.getMock();
		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		MockControl lobHandlerControl = MockControl.createControl(LobHandler.class);
		LobHandler lobHandler = (LobHandler) lobHandlerControl.getMock();
		MockControl lobCreatorControl = MockControl.createControl(LobCreator.class);
		LobCreator lobCreator = (LobCreator) lobCreatorControl.getMock();

		byte[] content = "content".getBytes();
		rs.findColumn("column");
		rsControl.setReturnValue(1);
		lobHandler.getBlobAsBytes(rs, 1);
		lobHandlerControl.setReturnValue(content);
		lobHandler.getLobCreator();
		lobHandlerControl.setReturnValue(lobCreator);
		lobCreator.setBlobAsBytes(ps, 1, content);
		lobCreatorControl.setVoidCallable(1);
		lobCreator.close();
		lobCreatorControl.setVoidCallable(1);

		rsControl.replay();
		psControl.replay();
		lobHandlerControl.replay();
		lobCreatorControl.replay();

		BlobByteArrayType type = new BlobByteArrayType(lobHandler);
		assertEquals(1, type.sqlTypes().length);
		assertEquals(Types.BLOB, type.sqlTypes()[0]);
		assertEquals(byte[].class, type.returnedClass());
		assertTrue(type.equals(new byte[] {(byte) 255}, new byte[] {(byte) 255}));
		assertTrue(Arrays.equals(new byte[] {(byte) 255}, (byte[]) type.deepCopy(new byte[] {(byte) 255})));
		assertTrue(type.isMutable());

		assertEquals(content, type.nullSafeGet(rs, new String[] {"column"}, null));
		TransactionSynchronizationManager.initSynchronization();
		try {
			type.nullSafeSet(ps, content, 1);
			List synchs = TransactionSynchronizationManager.getSynchronizations();
			assertEquals(1, synchs.size());
			((TransactionSynchronization) synchs.get(0)).beforeCompletion();
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}

		rsControl.verify();
		psControl.verify();
		lobHandlerControl.verify();
		lobCreatorControl.verify();
	}

}
