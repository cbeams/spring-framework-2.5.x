package org.springframework.jdbc.support.xml;

import java.io.Writer;

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
	 */
	void provideXml(Writer writer);

}
