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

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

/**
 * Implementation of <code>AbstractJasperReportsView</code> that renders
 * report results in XLS format.
 * @author robh
 * @see org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView
 */
public class JasperReportsExcelView extends
		AbstractOutputStreamDrivenJasperReportsView {

	/**
     * Sets the content type of this view to <code>application/vnd.ms-excel</code>.
	 */
	public JasperReportsExcelView() {
		setContentType("application/vnd.ms-excel");
	}
	
    /**
     * Returns an instance of <code>JRXlsExporter</code>
     */
	protected JRAbstractExporter getExporter() {
		return new JRXlsExporter();
	}

}
