package org.springframework.jdbc.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import junit.framework.TestCase;

import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.DefaultBlobImpl;
import org.springframework.jdbc.support.lob.BlobCreator;
import org.springframework.util.FileCopyUtils;

import org.easymock.MockControl;

/**
 * @author Juergen Hoeller
 * @since 17.12.2003
 */
public class DefaultLobHandlerTests extends TestCase {

	public void testCreateBlob() throws SQLException, IOException {
		BlobCreator blobCreator = (new DefaultLobHandler()).getBlobCreator();

		Blob blob = blobCreator.createBlob(null, "testContent".getBytes());
		assertTrue(blob instanceof DefaultBlobImpl);
		assertEquals(11, blob.length());
		assertEquals("testContent", new String(blob.getBytes(1, (int) blob.length())));

		blob = blobCreator.createBlob(null, "testContent".getBytes());
		InputStream bis = blob.getBinaryStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FileCopyUtils.copy(bis, bos);
		assertEquals("testContent", new String(bos.toByteArray()));

		blob = blobCreator.createBlob(null, new ByteArrayInputStream("testContent".getBytes()));
		assertTrue(blob instanceof DefaultBlobImpl);
		assertEquals(11, blob.length());
		assertEquals("testContent", new String(blob.getBytes(1, (int) blob.length())));
	}

	public void testSetBlobAsBytes() throws SQLException {
		BlobCreator blobCreator = (new DefaultLobHandler()).getBlobCreator();
		byte[] bytes = "testContent".getBytes();

		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		ps.setBytes(1, bytes);
		psControl.replay();

		blobCreator.setBlobAsBytes(ps, 1, bytes);
		psControl.verify();
	}

	public void testSetBlobAsBinaryStream() throws SQLException, IOException {
		BlobCreator blobCreator = (new DefaultLobHandler()).getBlobCreator();
		InputStream bis = new ByteArrayInputStream("testContent".getBytes());

		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		ps.setBinaryStream(1, bis, 11);
		psControl.replay();

		blobCreator.setBlobAsBinaryStream(ps, 1, bis);
		psControl.verify();
	}

}
