package org.springframework.jdbc.support.lob;

/**
 * Abstraction for handling LOBs (Large OBjects) in specific databases.
 * Its main purpose is to isolate Oracle's peculiar handling of LOBs in
 * OracleLobHandler; most other databases should work with DefaultLobHandler.
 *
 * <p>Currently just a factory for BlobCreator instances, to be used as sessions
 * for creating BLOBs. BlobCreators are typically instantiated for each statement
 * execution or for each transaction. They are not thread-safe because they might
 * track allocated database resources to be able to free them after execution.
 *
 * <p>Most databases should be able to work with the default BlobCreator as returned
 * by DefaultLobHandler. Unfortunately, Oracle just accepts Blob instances created
 * via its own proprietary BLOB API, and additionally doesn't accept large streams
 * for PreparedStatement.setBinaryStream. Therefore, you need to use OracleLobHandler
 * there, which uses Oracle's BLOB API for both createBlob and setBlobAsBinaryStream.
 *
 * @author Juergen Hoeller
 * @since 23.12.2003
 * @see DefaultLobHandler
 * @see OracleLobHandler
 */
public interface LobHandler {

	/**
	 * Create a new BlobCreator instance, i.e. a session for creating BLOBs.
	 * Needs to be closed after the created BLOBs are not needed anymore,
	 * i.e. after statement execution or transaction completion.
	 * @return the new BlobCreator instance
	 * @see BlobCreator#close
	 */
	BlobCreator getBlobCreator();

}
