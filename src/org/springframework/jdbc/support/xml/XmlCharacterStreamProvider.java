package org.springframework.jdbc.support.xml;

import java.io.Writer;
import java.io.IOException;

/**
 * Interface defining handling involved with providing <code>Writer</code>
 * data for XML input.
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see javax.xml.transform.Result
 */
public interface XmlCharacterStreamProvider {

	/**
	 * Implementations must implement this method to provide the XML content
	 * for the <code>Writer</code>.
	 * @param writer the <code>Writer</code> object being used to provide the XML input
	 * @throws IOException if an I/O error occurs while providing the XML
	 */
	void provideXml(Writer writer) throws IOException;

}
