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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.core.io.Resource;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * Base view class for all JasperReports views. Controls on the fly compilation
 * of report designs as required and is responsible for locating the report data
 * source in the Spring model.
 * 
 * @author robh
 */
public abstract class AbstractJasperReportsView extends AbstractUrlBasedView {

    /**
     * Key used to locate report data in <code>Collection</code> form 
     * in the model.
     */
    private static final String REPORT_DATA_KEY = "reportData";
    
    /**
     * The <code>JasperReport</code> that is used to render the view.
     */
    private JasperReport report;

    /**
     * A <code>Resource</code> instance that is the source of the
     * <code>JasperReport</code>.
     */
    private Resource reportResource;

    /**
     * Finds the <code>JRDataSource</code> to use for rendering the report and
     * then invokes the renderView() method that should be implemented by the
     * subclass.
     * 
     * @param model
     *            A <code>Map</code> containing the model data. Should contain
     *            an instance of <code>JRDataSource</code>.
     * @throws NoDataSourceException
     *             Thrown if the <code>model</code> parameter does not contain
     *             an instance of <code>JRDataSource</code>
     */
    protected final void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception, NoDataSourceException {

        JRDataSource dataSource = locateDataSource(model);

        if (dataSource == null) {
            throw new NoDataSourceException("No Report DataSource Supplied.");
        }

        response.setContentType(getContentType());
        renderView(report, model, dataSource, response);
    }

    /**
     * Checks to see that a valid report file URL is supplied in the
     * configuration. Compiles the report file is necessary.
     */
    protected void initApplicationContext()
            throws UnrecognizedReportExtensionException,
            JasperReportsInitializationException, ReportFileNotFoundException {
        super.initApplicationContext();

        String reportPath = getUrl();

        // we know the url is set
        // now try to get the report
        // and then compile it
        reportResource = getApplicationContext().getResource(getUrl());

        try {
            if (reportPath.endsWith(".jasper")) {
                report = JasperManager.loadReport(reportResource.getInputStream());
            } else if (reportPath.endsWith(".jrxml")) {
                // attempt a compile
                report = JasperReportsUtils.compileReport(reportResource.getInputStream());
            } else {
                throw new UnrecognizedReportExtensionException(
                        "Report URL must end in either .jasper or .jrxml");
            }
        } catch (JRException ex) {
            throw new JasperReportsInitializationException(
                    "An exception occured in the Jasper Reports framework during initialization.",
                    ex);
        } catch (FileNotFoundException ex) {
            throw new ReportFileNotFoundException(
                    "Unable to load report at path: " + getUrl());
        } catch (IOException ex) {
            throw new JasperReportsInitializationException(
                    "An exception occured whilst reading report data.", ex);
        }
    }

    /**
     * Attempts to locate an instance of <code>JRDataSource</code> in a given
     * <code>Map</code> instance. If no instance of <code>JRDataSource</code>
     * can be found, looks for an entry called <code>reportData</code> of type
     * <code>Collection</code> and creates an instance of
     * <code>JRDataSource</code> automatically.
     * 
     * @param model
     *            The <code>Map</code> to look in.
     * @return The <code>JRDataSource</code> if found, otherwise null.
     */
    private JRDataSource locateDataSource(Map model) {
        JRDataSource dataSource = null;

        for (Iterator i = model.values().iterator(); i.hasNext();) {
            Object o = i.next();

            if (o instanceof JRDataSource) {
                dataSource = (JRDataSource) o;
                break;
            }
        }
        
        // is the datasource still null
        if(dataSource == null) {
            Object data = model.get(REPORT_DATA_KEY);
            
            if(data instanceof Collection) {
                dataSource = new JRBeanCollectionDataSource((Collection)data);
            }
        }

        return dataSource;
    }

    /**
     * Subclasses should implement this method to perform the actual rendering
     * process.
     * 
     * @param report
     *            The <code>JasperReport</code> to render.
     * @param model
     *            The <code>Map</code> containg report parameters.
     * @param dataSource
     *            The <code>JRDataSource</code> containing the report data.
     * @param response
     *            The <code>HttpServletResponse</code> the report should be
     *            rendered to.
     * @throws Exception
     */
    protected abstract void renderView(JasperReport report, Map model,
            JRDataSource dataSource, HttpServletResponse response)
            throws Exception;

}