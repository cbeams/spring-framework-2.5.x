package org.springframework.jdbc.support.lob;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of the BlobCreator interface.
 * Creates instances of DefaultBlobImpl.
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see DefaultBlobImpl
 */
public class DefaultBlobCreator implements BlobCreator {

	protected final Log logger = LogFactory.getLog(getClass());

	public Blob createBlob(Connection con, byte[] content) {
		DefaultBlobImpl blob = new DefaultBlobImpl(content);
		logger.debug("Created new DefaultBlobImpl with length " + blob.length());
		return blob;
	}

	public Blob createBlob(Connection con, InputStream contentStream) {
		try {
			DefaultBlobImpl blob = new DefaultBlobImpl(contentStream);
			logger.debug("Created new DefaultBlobImpl with length " + blob.length());
			return blob;
		}
		catch (IOException ex) {
			throw new DataAccessResourceFailureException("Could not read InputStream for Blob", ex);
		}
	}

}
