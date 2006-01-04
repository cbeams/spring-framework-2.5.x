package org.springframework.scripting;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

/**
 * @author Rob Harrop
 */
public class ResourceScriptSource implements ScriptSource {

    private Resource resource;

    private long lastModified;

    public ResourceScriptSource(Resource resource) {
        this.resource = resource;
        this.lastModified = getLastModifiedTime(resource);
    }

    public InputStream getScript() throws IOException {
        this.lastModified = getLastModifiedTime(this.resource);
        return this.resource.getInputStream();
    }

    public boolean isModified() {
        long currentModificationTime = getLastModifiedTime(this.resource);
        return (currentModificationTime > this.lastModified);
    }

    private long getLastModifiedTime(Resource resource) {

        try {
            File file = resource.getFile();
            return file.lastModified();
        } catch (IOException ex) {
            // never cause a refresh in this case
            // it is unlikey that we even can if we cant get
            // the last modified date

            // TODO: log this error
            return Long.MIN_VALUE;
        }
    }
}
