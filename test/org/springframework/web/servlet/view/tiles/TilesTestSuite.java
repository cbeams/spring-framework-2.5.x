package org.springframework.web.servlet.view.tiles;

import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.view.ViewResolverTestSuite;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.mock.MockHttpServletResponse;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Test for the TilesView
 * @author Alef Arendsen
 * @version $RevisionId$
 */
public class TilesTestSuite extends TestCase {

    public TilesTestSuite(String name) {
        super(name);
    }

    public void testTilesViewResolver() throws Exception {
        StaticWebApplicationContext wac = new StaticWebApplicationContext() {
			protected InputStream getResourceByPath(String path) throws IOException {
				return ViewResolverTestSuite.class.getResourceAsStream(path);
			}
		};
        MockServletContext sCtx = new MockServletContext("", "/org/springframework/web/servlet/view/tiles/web.xml");
        wac.setServletContext(sCtx);
		TilesConfigurer tc = new TilesConfigurer();
		List files = new ArrayList();
        files.add("/org/springframework/web/servlet/view/tiles/tiles-test.xml");
        tc.setDefinitions(files);
        tc.setValidateDefinitions(true);
        tc.setFactoryClass("org.apache.struts.tiles.xmlDefinition.I18nFactorySet");
        tc.setApplicationContext(wac);

        InternalResourceViewResolver irvr = new InternalResourceViewResolver();
        irvr.setViewClass(TilesView.class);
        View v = irvr.resolveViewName("testTile", new Locale("nl"));
        assertEquals(v.getClass(), TilesView.class);
        assertEquals(v.getName(), "testTile");
    }
}
