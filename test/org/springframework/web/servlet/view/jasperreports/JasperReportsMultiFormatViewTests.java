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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rob Harrop
 */
public class JasperReportsMultiFormatViewTests extends AbstractJasperReportsViewTests {

	protected void extendModel(Map model) {
		model.put(getDiscriminatorKey(), "csv");
	}

	public void testSimpleHtmlRender() throws Exception {
		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);

		Map model = new HashMap();
		model.put("ReportTitle", "Foo");
		model.put("dataSource", getData());
		model.put(getDiscriminatorKey(), "html");

		view.render(model, request, response);

		assertEquals("Invalid content type", "text/html", response.getContentType());
	}

	public void testOverrideContentDisposition() throws Exception {
		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);

		Map model = new HashMap();
		model.put("ReportTitle", "Foo");
		model.put("dataSource", getData());
		model.put(getDiscriminatorKey(), "csv");

		String headerValue = "inline; filename=foo.txt";

		Properties mappings = new Properties();
		mappings.put("csv", headerValue);


		((JasperReportsMultiFormatView)view).setContentDispositionMappings(mappings);

		view.render(model, request, response);

		assertEquals("Invalid Content-Disposition header value", headerValue,
				response.getHeader("Content-Disposition"));
	}

	protected String getDiscriminatorKey() {
		return "format";
	}

	protected AbstractJasperReportsView getViewImplementation() {
		JasperReportsMultiFormatView view = new JasperReportsMultiFormatView();
		return view;
	}

	protected String getDesiredContentType() {
		return "text/csv";
	}

}
