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
		container.render(getUrl(), request, response);
	}

}
