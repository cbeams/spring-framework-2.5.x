/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view.jasperreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.jasperreports.PersonBean;
import org.springframework.ui.jasperreports.ProductBean;

/**
 * @author Rob Harrop
 * @since 18.11.2004
 */
public abstract class AbstractJasperReportsTests extends TestCase {

    private static final String COMPILED_REPORT =
            "org/springframework/ui/jasperreports/DataSourceReport.jasper";

    private static final String UNCOMPILED_REPORT =
            "org/springframework/ui/jasperreports/DataSourceReport.jrxml";

    private static final String SUB_REPORT_PARENT =
            "org/springframework/ui/jasperreports/subReportParent.jrxml";

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    protected abstract AbstractJasperReportsView getViewImplementation();

    protected abstract String getDesiredContentType();

    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * Simple test to see if compiled report succeeds.
     */
    public void testCompiledReport() throws Exception {
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        view.render(getModel(), request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testUncompiledReport() throws Exception {
        AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);
        view.render(getModel(), request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithInvalidPath() throws Exception {
        try {
            getView("foo.jasper");
            fail("Invalid path should throw ApplicationContextException");
        } catch (ApplicationContextException ex) {
            // good!
        }
    }

    public void testInvalidExtension() throws Exception {
        try {
            getView("foo.bar");
            fail("Invalid extension should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testContentType() throws Exception {
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        assertEquals("View content type is incorrect", getDesiredContentType(), view.getContentType());
        view.render(getModel(), request, response);
        assertEquals("Response content type is incorrect", getDesiredContentType(), response.getContentType());
    }

    public void testWithoutDatasource() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        try {
            AbstractJasperReportsView view = getView(COMPILED_REPORT);
            view.render(model, request, response);
            fail("No data source should result in NoDataSourceException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testWithCollection() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData());
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        view.render(model, request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithMultipleCollections() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData());
        model.put("otherData", new LinkedList());
        try {
            AbstractJasperReportsView view = getView(COMPILED_REPORT);
            view.render(model, request, response);
            fail("No data source should result in NoDataSourceException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testWithSpecificCollection() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData());
        model.put("otherData", new LinkedList());
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        view.setReportDataKey("reportData");
        view.render(model, request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithArray() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData().toArray());
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        view.render(model, request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithMultipleArrays() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData().toArray());
        model.put("otherData", new String[0]);
        try {
            AbstractJasperReportsView view = getView(COMPILED_REPORT);
            view.render(model, request, response);
            fail("No data source should result in NoDataSourceException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testWithSpecificArray() throws Exception {
        Map model = getModel();
        model.remove("dataSource");
        model.put("reportData", getData().toArray());
        model.put("otherData", new String[0]);
        AbstractJasperReportsView view = getView(COMPILED_REPORT);
        view.setReportDataKey("reportData");
        view.render(model, request, response);
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithSubReport() throws Exception {
        Map model = getModel();
        model.put("SubReportData", getProductData());

        Map subReports = new HashMap();
        subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChild.jrxml");

        List subReportDataSources = new ArrayList();
        subReportDataSources.add("SubReportData");

        AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
        view.setReportDataKey("dataSource");
        view.setSubReports(subReports);
        view.setSubReportDataSources(subReportDataSources);
        view.initApplicationContext();
        view.render(model, request, response);

        assertTrue(response.getContentAsByteArray().length > 0);
    }

    public void testWithNonExistentSubReport() throws Exception {
        Map model = getModel();
        model.put("SubReportData", getProductData());

        Map subReports = new HashMap();
        subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChildFalse.jrxml");

        List subReportDataSources = new ArrayList();
        subReportDataSources.add("SubReportData");

        AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
        view.setReportDataKey("dataSource");
        view.setSubReports(subReports);
        view.setSubReportDataSources(subReportDataSources);

        try {
            view.initApplicationContext();
            fail("Invalid report URL should throw ApplicationContext Exception");
        } catch(ApplicationContextException ex) {
            // success
        }
    }

    public void testSubReportWithUnspecifiedParentDataSource() throws Exception {
        Map model = getModel();
        model.put("SubReportData", getProductData());

        Map subReports = new HashMap();
        subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChildFalse.jrxml");

        List subReportDataSources = new ArrayList();
        subReportDataSources.add("SubReportData");

        AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
        view.setSubReports(subReports);
        view.setSubReportDataSources(subReportDataSources);

        try {
            view.initApplicationContext();
            fail("Unspecified reportDataKey should throw exception when subReportDataSources is specified");
        } catch(ApplicationContextException ex) {
            // success
        }
    }

    private AbstractJasperReportsView getView(String url) throws Exception {
        AbstractJasperReportsView view = getViewImplementation();
        view.setUrl(url);
        view.setApplicationContext(new StaticApplicationContext());
        return view;
    }

    private Map getModel() {
        Map model = new HashMap();
        model.put("ReportTitle", "Dear Lord!");
        model.put("dataSource", new JRBeanCollectionDataSource(getData()));
        return model;
    }

    private List getData() {
        List list = new ArrayList();
        for (int x = 0; x < 10; x++) {
            PersonBean bean = new PersonBean();
            bean.setId(x);
            bean.setName("Rob Harrop");
            bean.setStreet("foo");
            list.add(bean);
        }
        return list;
    }

    private List getProductData() {
        List list = new ArrayList();
        for(int x = 0; x < 10; x++) {
            ProductBean bean = new ProductBean();
            bean.setId(x);
            bean.setName("Foo Bar");
            bean.setPrice(1.9f);
            bean.setQuantity(1.0f);

            list.add(bean);
        }
        return list;
    }

}
