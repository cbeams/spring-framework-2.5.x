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
import net.sf.jasperreports.engine.export.JRCsvExporter;

/**
 * Implementation of AbstractJasperReportsView that renders
 * report results in CSV format.
 * @author robh
 */
public class JasperReportsCsvView extends AbstractWriterDrivenJasperReportsView {

    /**
     * Sets the content type of this report to <code>text/csv</code>
     *
     */
	public JasperReportsCsvView() {
		setContentType("text/csv");
	}

    /**
     * Returns an instance of <code>JRCsvExporter</code>
     * @return A <code>JRCsvExporter</code>
     */
	protected JRAbstractExporter getExporter() {
		return new JRCsvExporter();
	}

}
