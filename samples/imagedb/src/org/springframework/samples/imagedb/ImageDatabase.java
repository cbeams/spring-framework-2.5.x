package org.springframework.samples.imagedb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * Central business interface for the image database.
 * @author Juergen Hoeller
 * @since 08.01.2004
 */
public interface ImageDatabase {

	List getImages() throws DataAccessException;

	void streamImage(String name, OutputStream os) throws DataAccessException, IOException;

	void storeImage(String name, InputStream is, String description) throws DataAccessException, IOException;

	void checkImages() throws DataAccessException;

	void clearDatabase() throws DataAccessException;

}
