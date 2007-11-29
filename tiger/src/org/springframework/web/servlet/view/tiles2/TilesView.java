/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.servlet.view.tiles2;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.TilesContainer;
import org.apache.tiles.access.TilesAccess;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.util.WebUtils;

/**
 * View implementation that retrieves a Tiles definition.
 * The "url" property is interpreted as name of a Tiles definition.
 *
 * <p>This class builds on Tiles2, which requires Java 5 and JSP 2.0.
 * JSTL support is integrated out of the box.
 *
 * <p>Depends on a TilesContainer which must be available in
 * the ServletContext. This container is typically set up via a
 * {@link TilesConfigurer} bean definition in the application context.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #setUrl
 * @see TilesConfigurer
 */
public class TilesView extends AbstractUrlBasedView {

	private MessageSource jstlAwareMessageSource;


	protected void initApplicationContext() {
		super.initApplicationContext();
		this.jstlAwareMessageSource =
				JstlUtils.getJstlAwareMessageSource(getServletContext(), getApplicationContext());
	}

	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		TilesContainer container = TilesAccess.getContainer(getServletContext());
		exposeModelAsRequestAttributes(model, request);
		JstlUtils.exposeLocalizationContext(request, this.jstlAwareMessageSource);
		if (!response.isCommitted()) {
			// Tiles is going to use a forward, but some web containers (e.g. OC4J 10.1.3)
			// do not properly expose the Servlet 2.4 forward request attributes...
			WebUtils.exposeForwardRequestAttributes(request);
		}
		container.render(getUrl(), request, response);
	}

}
