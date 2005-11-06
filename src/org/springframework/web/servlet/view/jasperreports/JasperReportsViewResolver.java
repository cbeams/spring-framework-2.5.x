/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

import net.sf.jasperreports.engine.design.JRCompiler;

/**
 * {@link org.springframework.web.servlet.ViewResolver} implementation that resolves
 * instances of {@link AbstractJasperReportsView} by translating the
 * supplied view name into the URL of the report file.
 * 
 * @author Rob Harrop
 */
public class JasperReportsViewResolver extends UrlBasedViewResolver {

	/**
	 * A String key used to lookup the <code>JRDataSource</code> in the model.
	 */
	private String reportDataKey;

	/**
	 * Stores the paths to any sub-report files used by this top-level report,
	 * along with the keys they are mapped to in the top-level report file.
	 */
	private Properties subReportUrls;

	/**
	 * Stores the names of any data source objects that need to be converted to
	 * <code>JRDataSource</code> instances and included in the report parameters
	 * to be passed on to a sub-report.
	 */
	private String[] subReportDataKeys;

	/**
	 * Stores the headers to written with each response
	 */
	private Properties headers;

	/**
	 * Stores the exporter parameters passed in by the user as passed in by the user. May be keyed as
	 * <code>String</code>s with the fully qualified name of the exporter parameter field.
	 */
	private Map exporterParameters = new HashMap();

	/**
	 * Stores the <code>DataSource</code>, if any, used as the report data source.
	 */
	private DataSource jdbcDataSource;

	/**
	 * Holds the JRCompiler implementation to use for compiling reports on-the-fly.
	 */
	private JRCompiler reportCompiler;

	/**
	 * Sets the <code>reportDataKey</code> the view class should use.
	 * @see AbstractJasperReportsView#setReportDataKey
	 */
	public void setReportDataKey(String reportDataKey) {
		this.reportDataKey = reportDataKey;
	}

	/**
	 * Sets the <code>subReportUrls</code> the view class should use.
	 * @see AbstractJasperReportsView#setSubReportUrls
	 */
	public void setSubReportUrls(Properties subReportUrls) {
		this.subReportUrls = subReportUrls;
	}

	/**
	 * Sets the <code>subReportDataKeys</code> the view class should use.
	 * @see AbstractJasperReportsView#setSubReportDataKeys
	 */
	public void setSubReportDataKeys(String[] subReportDataKeys) {
		this.subReportDataKeys = subReportDataKeys;
	}

	/**
	 * Sets the <code>headers</code> the view class should use.
	 * @see AbstractJasperReportsView#setHeaders
	 */
	public void setHeaders(Properties headers) {
		this.headers = headers;
	}

	/**
	 * Sets the <code>exporterParameters</code> the view class should use.
	 * @see AbstractJasperReportsView#setExporterParameters
	 */
	public void setExporterParameters(Map exporterParameters) {
		this.exporterParameters = exporterParameters;
	}

	/**
	 * Sets the {@link DataSource} the view class should use.
	 * @see AbstractJasperReportsView#setJdbcDataSource
	 */
	public void setJdbcDataSource(DataSource jdbcDataSource) {
		this.jdbcDataSource = jdbcDataSource;
	}

	/**
	 * Sets the{@link JRCompiler} the view class should use.
	 * @see AbstractJasperReportsView#setReportCompiler
	 */
	public void setReportCompiler(JRCompiler reportCompiler) {
		this.reportCompiler = reportCompiler;
	}

	/**
	 * Checks to see whether the supplied class is a subclass of {@link AbstractJasperReportsView}. If so,
	 * delegates to the super class otherwise throws an {@link IllegalArgumentException}.
	 */
	public void setViewClass(Class viewClass) {
		if(!AbstractJasperReportsView.class.isAssignableFrom(viewClass)) {
			throw new IllegalArgumentException("Class [" + viewClass.getName() + "] is not a subclass of [" + AbstractJasperReportsView.class.getName() + "].");
		}
		super.setViewClass(viewClass);
	}

	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		AbstractJasperReportsView view = (AbstractJasperReportsView) super.buildView(viewName);
		view.setExporterParameters(this.exporterParameters);
		view.setHeaders(this.headers);
		view.setJdbcDataSource(this.jdbcDataSource);
		view.setReportDataKey(this.reportDataKey);
		view.setSubReportDataKeys(this.subReportDataKeys);
		view.setSubReportUrls(this.subReportUrls);

		if(this.reportCompiler != null) {
			view.setReportCompiler(this.reportCompiler);
		}

		return view;
	}
}
