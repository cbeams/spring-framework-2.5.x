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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;

/**
 * Jasper Reports view class that allows for the actual rendering format to be
 * specified at runtime using a parameter contained in the model.
 *
 * @author Rob Harrop
 */
public class JasperReportsMultiFormatView extends AbstractJasperReportsView {

	/**
	 * <code>Log</code> for this class.
	 */
	private static final Log log = LogFactory.getLog(JasperReportsMultiFormatView.class);

	/**
	 * Stores the key of the model parameter that corresponds to the
	 */
	private String discriminatorKey = "format";

	private Properties formatMappings;

	private Map mappings = new HashMap();

	public void setFormatMappings(Properties formatMappings) {
		this.formatMappings = formatMappings;
	}

	public void setDiscriminatorKey(String discriminatorKey) {
		this.discriminatorKey = discriminatorKey;
	}

	public void initJasperView() throws ApplicationContextException {
		if (formatMappings == null) {
			mappings.put("csv", JasperReportsCsvView.class);
			mappings.put("html", JasperReportsHtmlView.class);
			mappings.put("pdf", JasperReportsPdfView.class);
			mappings.put("xls", JasperReportsXlsView.class);
		}
		else {
			for (Enumeration en = formatMappings.keys(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				try {
					mappings.put(key, ClassUtils.forName(formatMappings.getProperty(key)));
				}
				catch (ClassNotFoundException ex) {
					throw new ApplicationContextException("Class [" + formatMappings.getProperty(key) +
							"] mapped to format [" + key + "] cannot be found.", ex);
				}
			}
		}
	}

	protected void renderReport(JasperReport report, Map model, JRDataSource dataSource,
			HttpServletResponse response) throws Exception {

		String format = (String) model.get(discriminatorKey);

		if (format == null) {
			throw new IllegalArgumentException("No format format found in model.");
		}

		Class viewClass = (Class) mappings.get(format);

		if (viewClass == null) {
			throw new IllegalArgumentException("Format discriminator [" + format + "] is not a configured mapping.");
		}

		AbstractJasperReportsView view = (AbstractJasperReportsView) BeanUtils.instantiateClass(viewClass);

		response.setContentType(view.getContentType());
		view.renderReport(report, model, dataSource, response);
	}


}
