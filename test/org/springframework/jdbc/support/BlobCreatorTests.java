package org.springframework.jdbc.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import junit.framework.TestCase;

import org.springframework.jdbc.support.lob.DefaultBlobCreator;
import org.springframework.jdbc.support.lob.DefaultBlobImpl;
import org.springframework.util.FileCopyUtils;

import org.easymock.MockControl;

/**
 * @author Juergen Hoeller
 * @since 17.12.2003
 */
public class BlobCreatorTests extends TestCase {

	public void testDefaultCreateBlob() throws SQLException, IOException {
		DefaultBlobCreator blobCreator = new DefaultBlobCreator();

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

	public void testDefaultSetBlobAsBinaryStream() throws SQLException, IOException {
		DefaultBlobCreator blobCreator = new DefaultBlobCreator();
		InputStream bis = new ByteArrayInputStream("testContent".getBytes());

		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		ps.setBinaryStream(1, bis, 11);
		psControl.replay();

		blobCreator.setBlobAsBinaryStream(ps, 1, bis);
		psControl.verify();
	}

}
