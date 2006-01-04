package org.springframework.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Rob Harrop
 */
public class StaticScriptSource implements ScriptSource {

    private String script;

    private boolean modified = true;

    public StaticScriptSource(String script) {
        this.script = script;
    }

    public void setScript(String script) {
        this.script = script;
        this.modified = true;
    }

    public InputStream getScript() throws IOException {
        this.modified = false;
        return new ReaderInputStream(new StringReader(this.script));
    }

    public boolean isModified() {
        return this.modified;
    }

    private static class ReaderInputStream extends InputStream {

        private Reader reader;

        public ReaderInputStream(Reader reader) {
            this.reader = reader;
        }

        public int read() throws IOException {
            return reader.read();
        }

        public void close() throws IOException {
            reader.close();
        }

        public long skip(long n) throws IOException {
            return reader.skip(n);
        }

        public synchronized void reset() throws IOException {
            reader.reset();
        }
    }
}
