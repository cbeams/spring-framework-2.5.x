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
 * <p/>
 * This view works on the concept of a discriminator key and a format key.
 * The discriminator key is used to pass the format key from your
 * <code>Controller</code> to Spring through as part of the model and the
 * format key is used to map a logical format to an actual JasperReports
 * view class. For example you might add the following code to your
 * <code>Controller</code>:
 * <pre>
 *     Map model = new HashMap();
 *     model.put("format", "pdf");
 * </pre>
 * Here <code>format</code> is the discriminator key and <code>pdf</code> is
 * the format key. When rendering a report, this class looks for a
 * model parameter under the disriminator key, which by default is
 * <code>format</code>. It then uses the value of this parameter to lookup
 * the actual <code>View</code> class to use. The default mappings for this
 * lookup are:
 * <ul>
 * <li><code>csv</code> - <code>JasperReportsCsvView</code></li>
 * <li><code>html</code> - <code>JasperReportsHtmlView</code></li>
 * <li><code>pdf</code> - <code>JasperReportsPdfView</code></li>
 * <li><code>xls</code> - <code>JasperReportsXlsView</code></li>
 * </ul>.
 * The discriminator key can be changed using the <code>discriminatorKey</code>
 * property and the format key to view class mappings can be changed using the
 * <code>formatMappings</code> property.
 *
 * @author Rob Harrop
 * @see #setDiscriminatorKey(String)
 * @see #setFormatMappings(java.util.Properties)
 */
public class JasperReportsMultiFormatView extends AbstractJasperReportsView {

	/**
	 * <code>Log</code> for this class.
	 */
	private static final Log log = LogFactory.getLog(JasperReportsMultiFormatView.class);

	/**
	 * Stores the key of the model parameter that holds the format key.
	 */
	private String discriminatorKey = "format";

	/**
	 * Stores the mapping of format keys to view class names.
	 * Configured by the user and converted by Spring into a
	 * <code>Map</code> of format keys to view <code>Class</code>es.
	 */
	private Properties formatMappings;

	/**
	 * Stores the coverted mappings, where each value has been converted
	 * from the <code>String</code> class name to the actual <code>Class</code>.
	 */
	private Map mappings = new HashMap();

	/**
	 * Sets the mappings of format discriminators to view class names.
	 */
	public void setFormatMappings(Properties formatMappings) {
		this.formatMappings = formatMappings;
	}

	/**
	 * Sets the discriminator key.
	 * @param discriminatorKey
	 */
	public void setDiscriminatorKey(String discriminatorKey) {
		this.discriminatorKey = discriminatorKey;
	}

	/**
	 * Converts the user-defined format mappings which map format discriminators to
	 * view class names in to internal mappings of format discriminators to view
	 * <code>Class</code>es. If no user-defined mappings are defined the default mappings are
	 * used.
	 * @throws ApplicationContextException if an invalid class name is found.
	 */
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

	/**
	 * Locates the format key in the model using the configured discriminator key and uses this
	 * key to lookup the appropriate view class from the mappings. The rendering of the
	 * report is then delegated to an instance of that view class.
	 *
	 * @param report the <code>JasperReport</code> to render
	 * @param dataSource the <code>JRDataSource</code> containing the report data
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
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
