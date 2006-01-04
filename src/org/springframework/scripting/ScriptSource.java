package org.springframework.scripting;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author Rob Harrop
 */
public interface ScriptSource {

    InputStream getScript() throws IOException;

    /**
     * Indicates whether the underlying script data was modified since the last time
     * #getScript was called.
     */
    boolean isModified();
}
