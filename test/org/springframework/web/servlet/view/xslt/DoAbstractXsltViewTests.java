/*
 * DoAbstractXsltViewTests.java
 */
 
package org.springframework.web.servlet.view.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import junit.framework.TestCase;

/**
 * @author Darren Davison
 * @since 1.2
 */
public class DoAbstractXsltViewTests extends TestCase {
    
    TestXsltView v;
    int errors;
    int warnings;
    int fatal;
    
    public void setUp() {
        v = new TestXsltView(); 
        errors = 0;
        warnings = 0;
        fatal = 0;
    }
    
    private void incWarnings() {
        warnings++;
    }
    
    private void incErrors() {
        errors++;
    }
    
    private void incFatals() {
        fatal++;
    }
    
    public void testNoSuchStylesheet() {
        v.setStylesheetLocation(new FileSystemResource("/does/not/exist.xsl"));
        try {
            v.initApplicationContext();
            fail("Should have thrown ApplicationContextException");
        } catch (ApplicationContextException e ) {
            // ok
        } catch (Exception e) {
            fail("Should have thrown ApplicationContextException");
        }
    }
    
    public void testChangeStylesheetReCachesTemplate() {
        v.setStylesheetLocation(new ClassPathResource("org/springframework/web/servlet/view/xslt/valid.xsl"));
        try {
            v.initApplicationContext();
        } catch (Exception e) {
            fail("stylesheet is valid");
        }

        try {
            v.setStylesheetLocation(new FileSystemResource("/does/not/exist.xsl"));
            fail("Should throw ApplicationContextException on re-caching template");
        } catch (ApplicationContextException e) {
            // ok
        } catch (Exception e) {
            fail("Should have thrown ApplicationContextException");
        }
        
    }
    
    public void testCustomErrorListener() {
        
        v.setErrorListener(new ErrorListener() {
            public void warning(TransformerException arg0) throws TransformerException {
                incWarnings();
            }
            public void error(TransformerException arg0) throws TransformerException {
                incErrors();
            }
            public void fatalError(TransformerException arg0) throws TransformerException {
                incFatals();
            }            
        });
        
        // loaded stylesheet is not well formed
        v.setStylesheetLocation(new ClassPathResource("org/springframework/web/servlet/view/xslt/errors.xsl"));
        v.initApplicationContext();
        assertEquals(1, fatal);
    }
    
    class TestXsltView extends AbstractXsltView {
        
    }
}
