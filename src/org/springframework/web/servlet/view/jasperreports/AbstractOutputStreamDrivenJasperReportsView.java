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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.springframework.ui.jasperreports.JasperReportsUtils;

/**
 * Abstract base class for Jasper Reports view that require
 * an <code>OutputStream</code> to write to.
 * @author robh
 */
public abstract class AbstractOutputStreamDrivenJasperReportsView extends
		AbstractJasperReportsView {

    /**
     * Subclasses should implement this method to define which
     * <code>JRAbstractExporter</code> should be used for report rendering.
     * The returned <code>JRAbstractExporter</code> should be capable of 
     * writing render output to an instance of <code>OutputStream</code>.
     * @return The <code>JRAbstractExporter</code> to use for rendering.
     */
	protected abstract JRAbstractExporter getExporter();
	
    /**
     * Fills the supplied <code>JasperReport</code> instance with the data from the
     * <code>JRDataSource</code> and <code>Map</code> objects and the renders the
     * result using the <code>JRAbstractExporter</code> obtained from getExporter() and 
     * writes it to the <code>HttpServletResponse</code>.
     * @see #getExporter()
     */
	protected void renderView(JasperReport report, Map model,
			JRDataSource dataSource, HttpServletResponse response)
			throws Exception {
		
		JasperPrint print = JasperReportsUtils.fillReport(report, model, dataSource);
	
		JasperReportsUtils.render(print, response.getOutputStream(), getExporter());
	}

}
