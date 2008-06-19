package org.springframework.jdbc.support.xml;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Interface defining handling involved with providing <code>OutputStream</code>
 * data for XML input.
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see javax.xml.transform.Result
 */
public interface XmlBinaryStreamProvider {

	/**
	 * Implementations must implement this method to provide the XML content
	 * for the <code>OutputStream</code>.
	 * @param outputStream the <code>OutputStream</code> object being used to provide the XML input
	 * @throws IOException if an I/O error occurs while providing the XML
	 */
	void provideXml(OutputStream outputStream) throws IOException;

}
