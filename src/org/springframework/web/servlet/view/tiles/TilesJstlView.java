package org.springframework.web.servlet.view.tiles;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

import org.springframework.web.servlet.support.JstlUtils;

/**
 * Specialization of TilesView for JSTL pages,
 * i.e. Tiles pages that use the JSP Standard Tag Library.
 *
 * <p>Exposes JSTL-specific request attributes specifying locale
 * and resource bundle for JSTL's formatting and message tags,
 * using Spring's locale and message source.
 *
 * <p>This is a separate class mainly to avoid JSTL dependencies
 * in TilesView itself.
 *
 * @author Juergen Hoeller
 * @since 20.08.2003
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 */
public class TilesJstlView extends TilesView {

	protected void exposeModelsAsRequestAttributes(Map model, HttpServletRequest request) throws ServletException {
		super.exposeModelsAsRequestAttributes(model, request);
		JstlUtils.exposeLocalizationContext(request, getApplicationContext());
	}

}
