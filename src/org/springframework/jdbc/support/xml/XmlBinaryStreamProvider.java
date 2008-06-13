package org.springframework.jdbc.support.xml;

import java.io.OutputStream;

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
	 */
	void provideXml(OutputStream outputStream);

}
