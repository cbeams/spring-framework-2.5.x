package org.springframework.web.multipart.support;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartFile;

/**
 * Custom PropertyEditor for converting MultipartFiles to byte arrays.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class ByteArrayMultipartFileEditor extends PropertyEditorSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) value;
			try {
				super.setValue(multipartFile.getBytes());
			}
			catch (IOException ex) {
				logger.error("Cannot read contents of multipart file", ex);
				throw new IllegalArgumentException("Cannot read contents of multipart file: " + ex.getMessage());
			}
		}
	}

}
